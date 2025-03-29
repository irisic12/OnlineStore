CREATE TABLE Category
(
	CategoryID BIGSERIAL,
	NameCategory TEXT NOT NULL,
	Description TEXT
);

ALTER TABLE Category 
ADD CONSTRAINT pk_category PRIMARY KEY(CategoryID);

ALTER TABLE Category
ADD CONSTRAINT unique_category UNIQUE(NameCategory);

----------------------------------------------------------------------

CREATE TABLE Product
(
	ProductID BIGSERIAL,
	NameProduct TEXT NOT NULL,
	Description TEXT,
	Price FLOAT NOT NULL,
	ImageURL TEXT,
	StockQuantity FLOAT,
	UnitOfMeasurement TEXT NOT NULL
);

ALTER TABLE Product 
ADD CONSTRAINT pk_product PRIMARY KEY(ProductID);

ALTER TABLE Product
ADD CONSTRAINT unique_product UNIQUE(NameProduct);

ALTER TABLE Product
ADD CONSTRAINT ch_price CHECK(Price > 0);

ALTER TABLE Product
ADD CONSTRAINT ch_quantity CHECK(StockQuantity >= 0);

ALTER TABLE Product
ADD Ref_Category BIGINT;
ALTER TABLE Product
ADD CONSTRAINT fk_category FOREIGN KEY(Ref_Category) REFERENCES Category(CategoryID);

---------------------------------------------------------------------

CREATE TABLE Customer
(
	CustomerID BIGSERIAL,
	FirstName TEXT NOT NULL,
	LastName TEXT NOT NULL,
	Email TEXT,
	Phone TEXT NOT NULL,
	Address TEXT NOT NULL,
	RegistrationDate date NOT NULL
);

ALTER TABLE Customer 
ADD CONSTRAINT pk_customer PRIMARY KEY(CustomerID);

ALTER TABLE Customer 
ADD CONSTRAINT ch_date CHECK(RegistrationDate <= CURRENT_DATE);

-----------------------------------------------------------------------

CREATE TABLE Orders
(
	OrderID BIGSERIAL,
	OrderDate date NOT NULL,
	TotalAmount FLOAT NOT NULL,
	OrderStatus TEXT NOT NULL,
	ShippingAddress TEXT NOT NULL,
	PaymentMethod TEXT NOT NULL
);

ALTER TABLE Orders 
ADD CONSTRAINT pk_orders PRIMARY KEY(OrderID);

ALTER TABLE Orders
ADD Ref_customer BIGINT;
ALTER TABLE Orders
ADD CONSTRAINT fk_customer FOREIGN KEY(Ref_customer) REFERENCES Customer(CustomerID);

ALTER TABLE Orders 
ADD CONSTRAINT ch_date CHECK(OrderDate <= CURRENT_DATE);

ALTER TABLE Orders
ADD CONSTRAINT ch_amount CHECK(TotalAmount > 0);

ALTER TABLE Orders 
ADD CONSTRAINT ch_status CHECK(OrderStatus = 'CREATED' OR OrderStatus = 'PAID' OR
	OrderStatus = 'IN_PROCESSING' OR OrderStatus = 'SENT' OR OrderStatus = 'DELIVERED' OR 
		OrderStatus = 'CANCELLED');

ALTER TABLE Orders
ADD CONSTRAINT ch_method CHECK(PaymentMethod = 'CREDIT_CARD' OR PaymentMethod = 'CASH');

----------------------------------------------------------------------------------------

CREATE TABLE OrderItem
(
	Quantity INTEGER NOT NULL
);

ALTER TABLE OrderItem
ADD CONSTRAINT ch_quantity CHECK(Quantity > 0);

ALTER TABLE OrderItem 
ADD Ref_order BIGINT;
ALTER TABLE OrderItem 
ADD CONSTRAINT fk_order FOREIGN KEY(Ref_order) REFERENCES Orders(OrderID);

ALTER TABLE OrderItem 
ADD Ref_product BIGINT;
ALTER TABLE OrderItem 
ADD CONSTRAINT fk_product FOREIGN KEY(Ref_product) REFERENCES Product(ProductID);

ALTER TABLE OrderItem 
ADD CONSTRAINT pk_item PRIMARY KEY(Ref_order, Ref_product);
