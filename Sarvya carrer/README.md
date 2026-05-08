# Sarvya Career OS

Engineering Career OS is a comprehensive platform designed for Indian students and aspiring engineers to master their tech careers. It includes expert-led courses, a dashboard for tracking progress, an AI assistant, and an ATS-optimized resume builder.

## Features

- **Expert Courses**: Industry-relevant content from engineers at major tech companies.
- **ATS-Optimized Resume Builder**: Create professional resumes with real-time preview and export capabilities.
- **Learning Dashboard**: Track your skills, streaks, and achievements.
- **AI-Powered Learning**: Get personalized assistance and career guidance.
- **Responsive & Modern UI**: Built with Next.js, Tailwind CSS, and Radix UI components for a premium feel.

## Tech Stack

- **Framework**: [Next.js 16](https://nextjs.org/)
- **Language**: [TypeScript](https://www.typescriptlang.org/)
- **Styling**: [Tailwind CSS](https://tailwindcss.com/)
- **Components**: [shadcn/ui](https://ui.shadcn.com/) (Radix UI)
- **Icons**: [Lucide React](https://lucide.dev/)
- **State Management**: React Hooks & Context API

## Getting Started

### Prerequisites

- Node.js (Latest LTS version recommended)
- [pnpm](https://pnpm.io/) (10.x or higher)

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/Sreejith-nair511/Software_Engine_miniProject.git
   cd Software_Engine_miniProject
   ```

2. Install dependencies:
   ```bash
   pnpm install
   ```

3. Run the development server:
   ```bash
   pnpm dev
   ```

4. Open [http://localhost:3000](http://localhost:3000) with your browser to see the result.

### Build

To create an optimized production build:
```bash
pnpm build
```

## Recent Fixes

### Resume Builder
- Fixed TypeScript type mismatches in state management.
- Improved real-time preview and input field responsiveness.
- Connected all interactive elements (Add/Remove experience).

### Core Components
- Fixed a critical `ThemeProvider` issue that prevented production builds by ensuring `ThemeContext` is always available during server-side rendering.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
