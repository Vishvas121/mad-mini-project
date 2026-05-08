import { NextResponse } from 'next/server';
import Groq from 'groq-sdk';

const groq = new Groq({
    apiKey: process.env.GROQ_API_KEY,
});

export async function POST(req: Request) {
    try {
        const { messages } = await req.json();

        const completion = await groq.chat.completions.create({
            messages: [
                {
                    role: 'system',
                    content: `You are an expert Engineering Career Coach. Your goal is to help users navigate their engineering career, from entry-level to staff/principal roles. 
          Provide advice on:
          - Resume building and portfolio design
          - Interview techniques (LeetCode, System Design, Behavioral)
          - Skills to learn for specific roles (Frontend, Backend, DevOps, AI/ML)
          - Career advancement and salary negotiation
          - Networking and job hunting strategies
          
          Be professional, encouraging, and provide actionable technical advice. Use markdown formatting for readability.`,
                },
                ...messages,
            ],
            model: 'llama-3.3-70b-versatile',
            temperature: 0.7,
            max_tokens: 2048,
            stream: true,
        });

        const stream = new ReadableStream({
            async start(controller) {
                for await (const chunk of completion) {
                    const content = chunk.choices[0]?.delta?.content || '';
                    controller.enqueue(new TextEncoder().encode(content));
                }
                controller.close();
            },
        });

        return new NextResponse(stream, {
            headers: {
                'Content-Type': 'text/plain; charset=utf-8',
            },
        });
    } catch (error: any) {
        console.error('Groq API Error:', error);
        return NextResponse.json({ error: 'Failed to generate response' }, { status: 500 });
    }
}
