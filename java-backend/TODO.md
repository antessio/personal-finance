# TODO

- [ ] Transaction time based ordered id
- [ ] Cash flow
- [ ] Insight section:
  - expenses insights
- [ ] Savings section
  - summary
    - ```sql
      select c.name, sum(t.amount) 
      from transactions t inner join categories c
      on c.id = t.category_id
      where t."date" >= '2025-10-01' and t."date" < '2025-11-01'
      and t."skip" = false
      and c.macro_category IN ('SAVINGS')
      group by c."name" ;```
  - trends
  - goals