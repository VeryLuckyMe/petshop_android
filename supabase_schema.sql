-- ────────────────────────────────────────────────────────────────
-- ZOOTOPIA / PAWMARKET OMNICHANNEL SQL MIGRATION
-- Paste this script directly in your Supabase SQL Editor to enable
-- the new Orders (with Shipping status) and Reviews tables.
-- ────────────────────────────────────────────────────────────────

-- 1. Create the ORDERS table (supports shipping status)
CREATE TABLE IF NOT EXISTS public.orders (
    id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    created_at timestamp with time zone DEFAULT timezone('utc'::text, now()) NOT NULL,
    user_email text NOT NULL,
    total_amount numeric NOT NULL,
    status text DEFAULT 'pending'::text NOT NULL, -- 'pending', 'packed', 'shipped', 'delivered', 'cancelled'
    shipping_address text NOT NULL,
    items jsonb NOT NULL -- Stores array of cart items: [{id, name, price, quantity, image_url}]
);

-- 2. Create the REVIEWS table
CREATE TABLE IF NOT EXISTS public.reviews (
    id bigserial PRIMARY KEY,
    created_at timestamp with time zone DEFAULT timezone('utc'::text, now()) NOT NULL,
    product_id integer NOT NULL,
    user_email text NOT NULL,
    username text NOT NULL,
    rating integer NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment text NOT NULL
);

-- 3. Security Definer Helper Function to check if caller is an Admin
-- (Using SECURITY DEFINER runs the query with the creator's privileges,
-- bypassing RLS to avoid infinite recursion when querying zootopiaDatabase)
CREATE OR REPLACE FUNCTION public.is_admin()
RETURNS boolean AS $$
BEGIN
  RETURN EXISTS (
    SELECT 1 FROM public."zootopiaDatabase"
    WHERE email = auth.jwt() ->> 'email' AND role = 'admin'
  );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 4. Configure Row Level Security (RLS) for Orders
ALTER TABLE public.orders ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Allow authenticated users to create orders" ON public.orders;
DROP POLICY IF EXISTS "Allow users to view their own orders" ON public.orders;
DROP POLICY IF EXISTS "Allow admins full access to orders" ON public.orders;

CREATE POLICY "Allow authenticated users to create orders" 
ON public.orders FOR INSERT 
TO authenticated 
WITH CHECK (auth.jwt() ->> 'email' = user_email);

CREATE POLICY "Allow users to view their own orders" 
ON public.orders FOR SELECT 
TO authenticated 
USING (auth.jwt() ->> 'email' = user_email);

CREATE POLICY "Allow admins full access to orders" 
ON public.orders FOR ALL 
TO authenticated 
USING (public.is_admin());

-- 5. Configure Row Level Security (RLS) for Reviews
ALTER TABLE public.reviews ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Allow public select on reviews" ON public.reviews;
DROP POLICY IF EXISTS "Allow authenticated users to write reviews" ON public.reviews;
DROP POLICY IF EXISTS "Allow admins to delete reviews" ON public.reviews;

CREATE POLICY "Allow public select on reviews" 
ON public.reviews FOR SELECT 
TO public 
USING (true);

CREATE POLICY "Allow authenticated users to write reviews" 
ON public.reviews FOR INSERT 
TO authenticated 
WITH CHECK (auth.jwt() ->> 'email' = user_email);

CREATE POLICY "Allow admins to delete reviews" 
ON public.reviews FOR DELETE 
TO authenticated 
USING (public.is_admin());

-- 6. Configure Row Level Security (RLS) for Products Inventory
ALTER TABLE public.products ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Allow public select on products" ON public.products;
DROP POLICY IF EXISTS "Allow admins full access to products" ON public.products;

CREATE POLICY "Allow public select on products" 
ON public.products FOR SELECT 
TO public 
USING (true);

CREATE POLICY "Allow admins full access to products" 
ON public.products FOR ALL 
TO authenticated 
USING (public.is_admin());

-- 7. Configure Row Level Security (RLS) for User Profiles (zootopiaDatabase)
ALTER TABLE public."zootopiaDatabase" ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Allow users to view their own profiles" ON public."zootopiaDatabase";
DROP POLICY IF EXISTS "Allow users to create their profiles" ON public."zootopiaDatabase";
DROP POLICY IF EXISTS "Allow users to update their own profiles" ON public."zootopiaDatabase";
DROP POLICY IF EXISTS "Allow admins full access to profiles" ON public."zootopiaDatabase";

CREATE POLICY "Allow users to view their own profiles" 
ON public."zootopiaDatabase" FOR SELECT 
TO authenticated 
USING (email = auth.jwt() ->> 'email');

CREATE POLICY "Allow users to create their profiles" 
ON public."zootopiaDatabase" FOR INSERT 
TO authenticated, anon 
WITH CHECK (email = auth.jwt() ->> 'email' OR auth.jwt() ->> 'email' IS NULL);

CREATE POLICY "Allow users to update their own profiles" 
ON public."zootopiaDatabase" FOR UPDATE 
TO authenticated 
USING (email = auth.jwt() ->> 'email')
WITH CHECK (email = auth.jwt() ->> 'email');

CREATE POLICY "Allow admins full access to profiles" 
ON public."zootopiaDatabase" FOR ALL 
TO authenticated 
USING (public.is_admin());

-- 8. Configure Row Level Security (RLS) for Appointments
ALTER TABLE public.appointments ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Allow users to view their own appointments" ON public.appointments;
DROP POLICY IF EXISTS "Allow users to book appointments" ON public.appointments;
DROP POLICY IF EXISTS "Allow admins full access to appointments" ON public.appointments;

CREATE POLICY "Allow users to view their own appointments" 
ON public.appointments FOR SELECT 
TO authenticated 
USING (auth.jwt() ->> 'email' = user_email);

CREATE POLICY "Allow users to book appointments" 
ON public.appointments FOR INSERT 
TO authenticated 
WITH CHECK (auth.jwt() ->> 'email' = user_email);

CREATE POLICY "Allow admins full access to appointments" 
ON public.appointments FOR ALL 
TO authenticated 
USING (public.is_admin());

-- 9. Configure Row Level Security (RLS) for Pets Registry
ALTER TABLE public.pets ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Allow users to view their own pets" ON public.pets;
DROP POLICY IF EXISTS "Allow users to manage their own pets" ON public.pets;

CREATE POLICY "Allow users to view their own pets" 
ON public.pets FOR SELECT 
TO authenticated 
USING (auth.uid() = user_id);

CREATE POLICY "Allow users to manage their own pets" 
ON public.pets FOR ALL 
TO authenticated 
USING (auth.uid() = user_id)
WITH CHECK (auth.uid() = user_id);

-- 10. Configure Row Level Security (RLS) for Addresses
ALTER TABLE public.addresses ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Allow users to view their own addresses" ON public.addresses;
DROP POLICY IF EXISTS "Allow users to manage their own addresses" ON public.addresses;

CREATE POLICY "Allow users to view their own addresses" 
ON public.addresses FOR SELECT 
TO authenticated 
USING (auth.uid() = user_id);

CREATE POLICY "Allow users to manage their own addresses" 
ON public.addresses FOR ALL 
TO authenticated 
USING (auth.uid() = user_id)
WITH CHECK (auth.uid() = user_id);

-- 11. Configure Row Level Security (RLS) for Carts
ALTER TABLE public.cart ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Allow users to view their own cart" ON public.cart;
DROP POLICY IF EXISTS "Allow users to manage their own cart" ON public.cart;

CREATE POLICY "Allow users to view their own cart" 
ON public.cart FOR SELECT 
TO authenticated 
USING (auth.jwt() ->> 'email' = user_email);

CREATE POLICY "Allow users to manage their own cart" 
ON public.cart FOR ALL 
TO authenticated 
USING (auth.jwt() ->> 'email' = user_email)
WITH CHECK (auth.jwt() ->> 'email' = user_email);

-- 12. Configure Row Level Security (RLS) for Wishlists
ALTER TABLE public.wishlist ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Allow users to view their own wishlist" ON public.wishlist;
DROP POLICY IF EXISTS "Allow users to manage their own wishlist" ON public.wishlist;

CREATE POLICY "Allow users to view their own wishlist" 
ON public.wishlist FOR SELECT 
TO authenticated 
USING (auth.uid() = user_id);

CREATE POLICY "Allow users to manage their own wishlist" 
ON public.wishlist FOR ALL 
TO authenticated 
USING (auth.uid() = user_id)
WITH CHECK (auth.uid() = user_id);

-- 13. Configure Row Level Security (RLS) for Recently Viewed Products
ALTER TABLE public.recently_viewed ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Allow users to view their own recently viewed" ON public.recently_viewed;
DROP POLICY IF EXISTS "Allow users to manage their own recently viewed" ON public.recently_viewed;

CREATE POLICY "Allow users to view their own recently viewed" 
ON public.recently_viewed FOR SELECT 
TO authenticated 
USING (auth.uid() = user_id);

CREATE POLICY "Allow users to manage their own recently viewed" 
ON public.recently_viewed FOR ALL 
TO authenticated 
USING (auth.uid() = user_id)
WITH CHECK (auth.uid() = user_id);

-- 14. Add missing 'description' column to the pre-existing 'products' table
ALTER TABLE public.products ADD COLUMN IF NOT EXISTS description text;
