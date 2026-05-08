import { SignIn } from "@clerk/nextjs";

export default function Page() {
    return (
        <div className="flex min-h-screen items-center justify-center bg-slate-950 px-4">
            <div className="absolute inset-0 -z-10 overflow-hidden">
                <div className="absolute top-20 left-10 w-72 h-72 bg-blue-500/10 rounded-full blur-3xl animate-pulse" />
                <div className="absolute bottom-20 right-10 w-72 h-72 bg-violet-500/10 rounded-full blur-3xl animate-pulse" style={{ animationDelay: '1s' }} />
            </div>

            <SignIn
                appearance={{
                    elements: {
                        formButtonPrimary: 'bg-gradient-to-r from-blue-500 to-blue-600 hover:from-blue-600 hover:to-blue-700 text-white font-semibold transition-all duration-300 transform border-none',
                        card: 'bg-slate-900 border border-white/10 shadow-2xl backdrop-blur-xl',
                        headerTitle: 'text-white',
                        headerSubtitle: 'text-slate-400',
                        socialButtonsBlockButton: 'bg-white/5 border-white/10 text-white hover:bg-white/10',
                        socialButtonsBlockButtonText: 'text-white font-medium',
                        formFieldLabel: 'text-slate-300',
                        formFieldInput: 'bg-white/5 border-white/10 text-white focus:border-blue-500/50',
                        footerActionText: 'text-slate-400',
                        footerActionLink: 'text-blue-400 hover:text-blue-300',
                        identityPreviewText: 'text-white',
                        identityPreviewEditButtonIcon: 'text-blue-400',
                    }
                }}
            />
        </div>
    );
}
