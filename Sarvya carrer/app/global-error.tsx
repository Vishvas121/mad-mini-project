'use client';

export default function GlobalError({
  error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  return (
    <html lang="en">
      <body style={{ margin: 0, background: '#020617', color: '#fff', fontFamily: 'sans-serif' }}>
        <div style={{
          minHeight: '100vh',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          gap: '16px',
          padding: '24px',
          textAlign: 'center',
        }}>
          <div style={{
            width: 64, height: 64, borderRadius: 16,
            background: 'linear-gradient(135deg, #ef4444, #dc2626)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            fontSize: 28,
          }}>
            ⚠️
          </div>
          <h1 style={{ fontSize: 24, fontWeight: 900, margin: 0 }}>Something went wrong</h1>
          <p style={{ color: '#94a3b8', margin: 0, maxWidth: 400 }}>
            An unexpected error occurred. Please try again.
          </p>
          {error?.digest && (
            <p style={{ color: '#475569', fontSize: 12, margin: 0 }}>
              Error ID: {error.digest}
            </p>
          )}
          <button
            onClick={reset}
            style={{
              padding: '10px 24px',
              background: 'linear-gradient(135deg, #6d28d9, #2563eb)',
              color: '#fff',
              border: 'none',
              borderRadius: 12,
              fontWeight: 700,
              fontSize: 14,
              cursor: 'pointer',
            }}
          >
            Try Again
          </button>
        </div>
      </body>
    </html>
  );
}
