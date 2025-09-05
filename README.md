# Hospital Service (Spring Boot + gRPC)

**What it does**
- Manage hospitals & patients, register patients to hospitals
- Track stays; create/cancel stays; quarter summaries
- Bills = total stay days × 200; list bills & outstanding balance

**Quickstart**
```bash
./gradlew bootRun              # gRPC :9090, HTTP :8080 (H2 console)
# or choose ports:
./gradlew bootRun --args="--server.port=8081 --grpc.server.port=9090"


├─ README.md                # Quickstart & overview
├─ docs/
│  ├─ architecture.md       # Deep dive: modules, ports, tech stack
│  ├─ api/                  # Generated API docs from .proto
│  │  └─ hospital.html      # (protoc-gen-doc output)
│  ├─ erd.md                # Data model / ER diagrams
│  ├─ adr/                  # Architecture Decision Records (optional)
│  │  └─ 0001-use-grpc.md
├─ CONTRIBUTING.md          # How to develop, branch naming, PRs
├─ LICENSE                  # Project license (if open source)
└─ src/main/proto           # Your .proto files (source of truth)
