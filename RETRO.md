# Sprint Retros

One line per sprint, per Section 16: what worked, what didn't, one change
for next sprint.

## Sprint 0 (scaffold)
- What worked: generating the full layered skeleton (model/dao/service/controller)
  in one pass kept package boundaries consistent from the start.
- What didn't: N/A yet - first sprint.
- Change for next sprint: run `mvn -B clean verify` against a real Tomcat/H2
  setup as soon as possible to catch integration issues the skeleton can't
  surface on its own.
