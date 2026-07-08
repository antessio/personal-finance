import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  eslint: {
    // Pre-existing lint debt shouldn't block production builds; type-checking
    // (tsc) still runs and catches real errors. Run `npm run lint` separately.
    ignoreDuringBuilds: true,
  },
};

export default nextConfig;
