# JB Chat — AI Workspace

This bundle contains a working AI chat app with server-side OpenAI calls, persistent browser chat history, agent-specific prompts, AI-generated HTML previews, and local project records. Chat and project data stay in the browser's local storage. The API key stays on the server.

## Vercel
1. Upload these files to a GitHub repository.
2. Import that repository into Vercel.
3. In Vercel Project Settings → Environment Variables, add `OPENAI_API_KEY` and optionally `OPENAI_MODEL` (default: `gpt-5.3`).
4. Redeploy, then open the deployment URL. The API key is never needed in the browser.

The OpenAI API key is server-side only and is not included in this package.

## Local
Node.js 20+ and a Vercel account for `vercel dev`:
```bash
npm install
npm start
```

## Current scope

- Chat history and project names persist in the current browser only.
- The Coding, Design, Research, and Testing agents call the AI API with focused instructions.
- Builder actions call the AI API and render the returned HTML in a sandboxed preview.
- Authentication, shared cloud storage, file uploads, and actual autonomous code deployment are not included.
