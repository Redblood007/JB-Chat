# JB Chat Android wrapper

This Android Studio project opens the existing JB Chat website inside a WebView. It does not bundle or expose the OpenAI API key.

## Open

Open this folder in Android Studio: `outputs/JBChatAndroid`. If prompted, let Android Studio sync Gradle and install the Android SDK platform/build tools it requests, then select an emulator or connected Android phone and press Run.

## Important

The website currently uses Vercel authentication protection, so Android users may be asked to sign in. Chat requests also require a valid `OPENAI_API_KEY` in the Vercel **Production** environment. The current deployed API key has been returning 401, so the app will show that error until the Production key is replaced and redeployed.
