from http.server import BaseHTTPRequestHandler
import os
import json
import base64
import requests
import google.generativeai as genai

GEMINI_API_KEY = os.getenv("GEMINI_API_KEY")
GITHUB_TOKEN = os.getenv("GITHUB_TOKEN")
GITHUB_REPO = os.getenv("GITHUB_REPO")
TARGET_SITE = os.getenv("TARGET_SITE", "https://passive-income-ten.vercel.app")

if GEMINI_API_KEY:
    genai.configure(api_key=GEMINI_API_KEY)

class handler(BaseHTTPRequestHandler):
    def do_GET(self):
        try:
            # Step 1: AI Content Generation
            model = genai.GenerativeModel("gemini-1.5-flash")
            prompt = f"""
            Act as an SEO Specialist. Create a trending, high-traffic commercial blog post for website {TARGET_SITE}.
            Return JSON only:
            {{
                "title": "SEO Optimized Click-Worthy Title",
                "slug": "trending-topic-guide",
                "meta_description": "155 chars meta description with call to action",
                "markdown_body": "# Title\\n\\nIntroduction...\\n\\n## Key Insights\\n\\n## Conclusion"
            }}
            """
            response = model.generate_content(
                prompt,
                generation_config={"response_mime_type": "application/json"}
            )
            article_data = json.loads(response.text)
            slug = article_data.get("slug", "daily-seo-update")
            title = article_data.get("title", "Daily SEO Update")
            body = article_data.get("markdown_body", "")

            # Step 2: Auto-Commit directly to GitHub
            if GITHUB_TOKEN and GITHUB_REPO:
                file_path = f"content/posts/{slug}.md"
                api_url = f"https://api.github.com/repos/{GITHUB_REPO}/contents/{file_path}"
                headers = {
                    "Authorization": f"Bearer {GITHUB_TOKEN}",
                    "Accept": "application/vnd.github.v3+json"
                }

                file_content = f"""---
title: "{title}"
slug: "{slug}"
date: "2026-09-30"
description: "{article_data.get('meta_description', '')}"
---

{body}
"""
                content_b64 = base64.b64encode(file_content.encode("utf-8")).decode("utf-8")

                sha = None
                check = requests.get(api_url, headers=headers)
                if check.status_code == 200:
                    sha = check.json().get("sha")

                commit_payload = {
                    "message": f"🤖 Vercel Auto-SEO: Published '{slug}' [skip ci]",
                    "content": content_b64,
                    "branch": "main"
                }
                if sha:
                    commit_payload["sha"] = sha

                commit_res = requests.put(api_url, headers=headers, json=commit_payload)
                commit_status = commit_res.status_code
            else:
                commit_status = "NO_TOKEN"

            # Step 3: Ping Search Engines
            requests.get(f"https://www.google.com/ping?sitemap={TARGET_SITE}/sitemap.xml", timeout=5)

            self.send_response(200)
            self.send_header('Content-type', 'application/json')
            self.end_headers()
            self.wfile.write(json.dumps({
                "status": "SUCCESS",
                "message": "Auto SEO Published and applied to website!",
                "slug": slug,
                "github_status": commit_status
            }).encode())

        except Exception as e:
            self.send_response(500)
            self.send_header('Content-type', 'application/json')
            self.end_headers()
            self.wfile.write(json.dumps({"status": "ERROR", "error": str(e)}).encode())
