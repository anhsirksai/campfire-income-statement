import type { NextConfig } from "next";

const backendUrl = process.env.BACKEND_URL ?? "http://localhost:8080";

const nextConfig: NextConfig = {
  cacheComponents: true,
  partialPrefetching: true,
  // The browser calls /api/...; Next forwards it to the Spring Boot backend, so no CORS is needed.
  async rewrites() {
    return [{ source: "/api/:path*", destination: `${backendUrl}/:path*` }];
  },
};

export default nextConfig;
