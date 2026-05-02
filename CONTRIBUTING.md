# Contributing

FoodSecurityNet welcomes public-interest contributions that help nonprofits, universities, researchers, students, civic technologists, and community organizations reduce software costs and work with food security data more responsibly.

## Good Contributions

Useful contributions include:

- Bug fixes.
- Documentation improvements.
- Accessibility fixes.
- Security hardening.
- Privacy and compliance notes.
- Tests for existing behavior.
- Local development improvements.
- Clearer examples using public, synthetic, or properly licensed data.
- Small features that support the existing food security workflow.

Avoid adding:

- Proprietary datasets without clear permission.
- Real personal, farm, household, or community data.
- Undocumented external service dependencies.
- AI claims that are not supported by code, tests, or evaluation.
- Production compliance claims without evidence.

## Development Setup

The shortest local path is:

```bash
npm install
node demo-server.js
```

Then in another terminal:

```bash
cd frontend
npm install
npm run dev
```

For the AI model service:

```bash
cd ai-model
pip install -r requirements.txt
python trainer.py
python agri_predictor.py
```

## Testing

Run the tests relevant to the files you changed.

Frontend:

```bash
cd frontend
npm run build
npm run lint
```

Auth service:

```bash
cd backend/auth-service
mvn test
```

End-to-end scaffold:

```bash
cd tests/e2e
pip install -r requirements.txt
pytest
```

AI model smoke checks are documented in `ai-model/TESTING.md`.

## Code And Documentation Guidelines

- Keep changes focused.
- Prefer clear names and plain language.
- Do not commit secrets, tokens, private keys, real credentials, or production data.
- Mark demo, mock, synthetic, or placeholder behavior clearly.
- Add tests when changing behavior.
- Update documentation when setup, features, compliance posture, or deployment assumptions change.
- Keep accessibility in mind for frontend changes: keyboard support, labels, focus behavior, contrast, reduced motion, and screen-reader announcements.

## Security And Privacy

For security issues, follow `SECURITY.md`.

For privacy-sensitive work, follow `PRIVACY.md` and avoid using real user, farm, household, student, institutional, or community data in examples or tests.

## License

By contributing, you agree that your contribution will be licensed under the MIT License unless the project maintainers explicitly state otherwise in writing.
