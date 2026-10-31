import OpenAI from "openai";

export default async function handler(req, res) {
  if (req.method !== "POST") {
    return res.status(405).json({ error: "Method not allowed." });
  }

  if (!process.env.OPENAI_API_KEY) {
    return res.status(503).json({
      error: "OPENAI_API_KEY is not configured on the server."
    });
  }

  try {
    const { message, previousResponseId, task } = req.body || {};

    if (typeof message !== "string" || !message.trim()) {
      return res.status(400).json({ error: "message is required." });
    }

    const client = new OpenAI({ apiKey: process.env.OPENAI_API_KEY });

    const taskInstructions = {
      chat: "You are JB Chat, a helpful AI assistant. Be concise, practical, and friendly.",
      coding: "You are the JB Chat coding agent. Analyze the user's request and return a practical implementation plan or code. State assumptions clearly.",
      design: "You are the JB Chat design agent. Give a specific visual design direction, layout, colors, typography, and accessibility guidance.",
      research: "You are the JB Chat research agent. Structure the answer as findings, assumptions, and actionable next steps. Do not invent sources.",
      testing: "You are the JB Chat QA agent. Identify risks and propose concrete checks and edge cases. Do not claim checks were run.",
      builder: "Create a complete, responsive, self-contained HTML page for the user's request. Return only the HTML document, with CSS and JavaScript inline."
    };
    const response = await client.responses.create({
      model: process.env.OPENAI_MODEL || "gpt-5.3",
      instructions: taskInstructions[task] || taskInstructions.chat,
      input: [{ role: "user", content: message.trim() }],
      ...(typeof previousResponseId === "string" && previousResponseId.trim()
        ? { previous_response_id: previousResponseId.trim() }
        : {})
    });

    return res.status(200).json({
      content: response.output_text || "I could not generate a response.",
      responseId: response.id
    });
  } catch (err) {
    console.error(err);
    return res.status(500).json({
      error: err?.message || "AI request failed."
    });
  }
}
