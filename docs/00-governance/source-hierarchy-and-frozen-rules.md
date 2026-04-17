# Meridian Source Hierarchy and Frozen Rules

## Source Hierarchy

1. Frozen architecture documents = primary design authority
2. Execution control artifact = day-to-day build authority
3. Theory and coding workbooks = slice-specific learning support only
4. Consolidated blueprint = umbrella clarity only

## Build Boundary

Meridian is a merchant operations platform used to onboard merchants, enable products, investigate transaction issues, provide statements and reports, manage chargebacks, support PCI-related follow-up, run support workflows, enforce sensitive-action controls, and coordinate external integrations. It does not replace external processors or become the source of raw payment or settlement truth. Integration work must stay behind Integration Edge. Support workflows must not become the hidden source of truth for other business domains. Lifecycle actions should be modeled explicitly, not reduced to generic CRUD. Derived views, search surfaces, dashboards, and projections are useful, but they are never canonical truth.

## Non-Negotiable Rules

- No direct vendor or external-system calls from business modules
- No cross-module direct writes into another module’s canonical data
- No generic CRUD for lifecycle-heavy flows
- No support-owned state replacing the owning module’s state
- No projection, search table, or dashboard acting as the write model
- No sensitive correction, reveal, override, or evidence action without proper control and traceability
