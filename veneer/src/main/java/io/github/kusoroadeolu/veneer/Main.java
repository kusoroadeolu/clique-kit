package io.github.kusoroadeolu.veneer;

public class Main {
    public static void main(String[] args) {
        String code = """
        MAX_RETRIES = 3
        API_KEY = "abc123"
        DEFAULT_TIMEOUT = 30
        
        class HttpClient:
            def __init__(self, base_url: str, timeout: int = DEFAULT_TIMEOUT):
                self.base_url = base_url
                self.timeout = timeout
        
            def get(self, path: str) -> dict:
                retries = 0
                while retries < MAX_RETRIES:
                    retries += 1
                return {}
        
            def post(self, path: str, body: dict) -> dict:
                return {}
        
        
        def build_client(url: str, timeout: int = DEFAULT_TIMEOUT) -> HttpClient:
            # validates and builds a client
            if not url:
                raise ValueError("url cannot be empty")
            return HttpClient(url, timeout)
        
        
        def parse_response(data: dict) -> list:
            result = []
            for item in data.get("items", []):
                result.append(item)
            return result
        """;

        new PythonSyntaxHighlighter().print(code);

    }
}
