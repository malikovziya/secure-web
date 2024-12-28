-- DB and TABLES ------------------------------------------------------------------
create database mysqlAnalytics;
use mysqlAnalytics;
CREATE TABLE sales(
    sales_employee VARCHAR(50) NOT NULL,
    fiscal_year INT NOT NULL,
    sale DECIMAL(14,2) NOT NULL,
    PRIMARY KEY(sales_employee,fiscal_year)
);

INSERT INTO sales(sales_employee,fiscal_year,sale)
VALUES('Bob',2016,100),
      ('Bob',2017,150),
      ('Bob',2018,200),
      ('Alice',2016,150),
      ('Alice',2017,100),
      ('Alice',2018,200),
      ('John',2016,200),
      ('John',2017,150),
      ('John',2018,250);

SELECT * FROM sales;

-- PARTITION BY clause ------------------------------------------------------
SELECT fiscal_year, sales_employee, sale, 
   SUM(sale) OVER (PARTITION BY fiscal_year) total_sales 
FROM 
   sales; 

-- ROW_NUMBER() FUNCTION ---------------------------------------------------
SELECT sales.fiscal_year, sales.sales_employee, sales.sale,
    ROW_NUMBER() OVER(PARTITION BY fiscal_year) as row_num
FROM 
    mysqlAnalytics.sales;

-- different syntax
SELECT fiscal_year, sales_employee, sale,
    ROW_NUMBER() OVER W as row_num
FROM 
    sales
WINDOW W as(PARTITION BY fiscal_year order by sale desc);

-- DENSE_RANK() FUNCTION ---------------------------------------------------
SELECT 
  sales_employee, 
  fiscal_year, 
  sale, 
  DENSE_RANK() OVER (
    PARTITION BY fiscal_year 
    ORDER BY sale DESC
  ) sales_rank 
FROM 
  sales;

-- RANK() and DENSE_RANK() together ---------------------------------------
SELECT 
  sales_employee, 
  fiscal_year, 
  sale, 
  DENSE_RANK() OVER (
    PARTITION BY fiscal_year 
    ORDER BY sale DESC
  ) sales_Drank,
  RANK() OVER (
    PARTITION BY fiscal_year 
    ORDER BY sale DESC
  ) sales_rank   
FROM 
  sales;

-- PERCENT_RANK() over the partition ----------------------------------------
SELECT 
  sales_employee, 
  fiscal_year, 
  sale, 
  PERCENT_RANK() OVER (
    PARTITION BY fiscal_year 
    ORDER BY sale
  ) percentile 
FROM 
  sales;

-- LAG() FUNCTION ------------------------------------------------------------
SELECT 
  sales_employee, 
  fiscal_year, 
  sale, 
  LAG(sale, 2 , 0) OVER (
    PARTITION BY sales_employee 
    ORDER BY fiscal_year
  ) 'previous year sale' 
FROM 
  sales;

-- multiply LAG() functions --------------------------------------------------
SELECT 
  sales_employee, 
  fiscal_year, 
  sale, 
  LAG(sale, 1, 0) OVER (
    PARTITION BY sales_employee 
    ORDER BY fiscal_year
  ) AS previous_year_sale,
  sale - LAG(sale, 1, 0) OVER (
    PARTITION BY sales_employee 
    ORDER BY fiscal_year
  ) AS vs_previous_year
FROM 
  sales;

-- FIRST_VALUE() FUNCTION ----------------------------------------------------
SELECT 
  sales_employee, 
  fiscal_year, 
  sale, 
  FIRST_VALUE(sales_employee) OVER (
    PARTITION BY fiscal_year
    ORDER BY sale
    ) 'min_sale' 
FROM 
    sales;

-- test MIN and MAX together --------------------------------------------------
SELECT 
  sales_employee, 
  fiscal_year, 
  sale, 
  FIRST_VALUE(sale) OVER (
    PARTITION BY fiscal_year
    ORDER BY sale ASC
    ) 'min_sale' ,
  FIRST_VALUE(sale) OVER (
    PARTITION BY fiscal_year
    ORDER BY sale DESC
    ) 'max_sale' 
FROM 
    sales;