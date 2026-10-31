# JB Chat Android app

This Android Studio project provides a native ChatGPT-style conversation screen, prompt suggestions, saved current conversation, and a new-chat button. AI requests go to the JB Chat server endpoint. It does not bundle or expose the OpenAI API key.

## Open

Open this folder in Android Studio: `outputs/JBChatAndroid`. If prompted, let Android Studio sync Gradle and install the Android SDK platform/build tools it requests, then select an emulator or connected Android phone and press Run.

## Important

The app calls `https://jb-chat-omega.vercel.app/api/chat`. The endpoint must be publicly reachable, and chat requests require a valid `OPENAI_API_KEY` in the Vercel **Production** environment. The current deployed key has returned 401, so AI replies will fail until the Production key is replaced and redeployed.
