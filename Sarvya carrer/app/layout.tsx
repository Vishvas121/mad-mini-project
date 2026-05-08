import { ClerkProvider } from '@clerk/nextjs'
import type { Metadata, Viewport } from 'next'
import { ThemeProvider } from './providers'

import './globals.css'

export const metadata: Metadata = {
  title: 'Engineering Career OS - Master Your Tech Career',
  description: 'Transform your engineering career with AI-powered learning, personalized roadmaps, and industry-leading tools for professional growth.',
  keywords: ['engineering', 'learning', 'career', 'AI', 'courses', 'resume builder'],
  authors: [{ name: 'Engineering Career OS' }],
  openGraph: {
    type: 'website',
    locale: 'en_US',
    url: 'https://engineeringcareeros.com',
    siteName: 'Engineering Career OS',
    title: 'Engineering Career OS - Master Your Tech Career',
    description: 'Transform your engineering career with AI-powered learning, personalized roadmaps, and industry-leading tools for professional growth.',
    images: [{ url: 'https://engineeringcareeros.com/og-image.png', width: 1200, height: 630 }],
  },
  twitter: {
    card: 'summary_large_image',
    title: 'Engineering Career OS - Master Your Tech Career',
    description: 'Transform your engineering career with AI-powered learning, personalized roadmaps, and industry-leading tools for professional growth.',
  },
}

export const viewport: Viewport = {
  width: 'device-width',
  initialScale: 1,
  maximumScale: 1,
  userScalable: false,
  themeColor: '#1e3a8a',
}

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode
}>) {
  return (
    <ClerkProvider>
      <html lang="en">
        <body className="font-sans antialiased">
          <ThemeProvider>
            {children}
          </ThemeProvider>
        </body>
      </html>
    </ClerkProvider>
  )
}
