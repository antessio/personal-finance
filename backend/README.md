# PersonalFinance

To start your Phoenix server:

  * Run `mix setup` to install and setup dependencies
  * Start Phoenix endpoint with `mix phx.server` or inside IEx with `iex -S mix phx.server`

Now you can visit [`localhost:4000`](http://localhost:4000) from your browser.

Ready to run in production? Please [check our deployment guides](https://hexdocs.pm/phoenix/deployment.html).

## Learn more

  * Official website: https://www.phoenixframework.org/
  * Guides: https://hexdocs.pm/phoenix/overview.html
  * Docs: https://hexdocs.pm/phoenix
  * Forum: https://elixirforum.com/c/phoenix-forum
  * Source: https://github.com/phoenixframework/phoenix




## Export

Export expense: 

```sql
SELECT 
    t.date, 
    CASE WHEN t.amount < 0 THEN 'Expense' ELSE 'Income' END AS type,
	INITCAP(REPLACE(c.macro_category, 'expense', 'Expense Var.')) AS macro_category,
    UPPER(c.emoji || ' ' || c."name") AS category, 
    '€',
    TO_CHAR(ABS(t.amount), 'FM999999999.00') AS amount, 
    t.description  
FROM transactions t 
INNER JOIN transactions_categories tc ON tc.transaction_id = t.id  
INNER JOIN categories c ON tc.category_id = c.id 
WHERE t."skip" = false
and c.macro_category <> 'savings'
and t."date" > '2025-01-31' and t."date" < '2025-03-01'
order by "date" ;
```


Export savings:

```sql
SELECT 
    t.date,
    UPPER(c.emoji || ' ' || c."name") AS category, 
    '€',
    TO_CHAR(ABS(t.amount), 'FM999999999.00') AS amount
FROM transactions t 
INNER JOIN transactions_categories tc ON tc.transaction_id = t.id  
INNER JOIN categories c ON tc.category_id = c.id 
WHERE t."skip" = false
and c.macro_category = 'savings'
and t."date" > '2025-01-31' and t."date" < '2025-03-01'
order by "date" ;

```