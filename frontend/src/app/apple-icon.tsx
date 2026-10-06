import { ImageResponse } from 'next/og';

export const size = { width: 180, height: 180 };
export const contentType = 'image/png';

// iOS home-screen icon (iOS applies its own rounded corners).
export default function AppleIcon() {
  return new ImageResponse(
    (
      <div
        style={{
          width: '100%',
          height: '100%',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          background: '#1976d2',
          color: 'white',
          fontSize: 108,
          fontWeight: 700,
        }}
      >
        €
      </div>
    ),
    size
  );
}
