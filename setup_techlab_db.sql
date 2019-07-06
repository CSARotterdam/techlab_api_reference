--
-- Table structure for table "inventory"
--

DROP TABLE IF EXISTS "inventory" CASCADE;
CREATE TABLE "inventory"
(
    "id"                varchar(36) NOT NULL UNIQUE,
    "name"              text        NOT NULL,
    "manufacturer"      text        NOT NULL,
    "category"          varchar(64) NOT NULL,
    "loan_time_in_days" integer     NOT NULL
);

-- --------------------------------------------------------

--
-- Table structure for table "inventoryMutation"
--

DROP TABLE IF EXISTS "inventoryMutation" CASCADE;
CREATE TABLE "inventoryMutation"
(
    "mutation_id"  varchar(36) NOT NULL UNIQUE,
    "inventory_id" varchar(36) NOT NULL,
    "type"         varchar(64) NOT NULL,
    "subtype"      varchar(64) NOT NULL,
    "loan_id"      varchar(36) DEFAULT NULL,
    "amount"       integer     NOT NULL,
    "time"         timestamp   NOT NULL
);

-- --------------------------------------------------------

--
-- Table structure for table "user"
--

DROP TABLE IF EXISTS "user" CASCADE;
CREATE TABLE "user"
(
    "id"            varchar(36) NOT NULL UNIQUE,
    "code"          varchar(36) NOT NULL,
    "mail"          text        NOT NULL,
    "mobile_number" text        NOT NULL,
    "name"          text        NOT NULL
);

-- --------------------------------------------------------

--
-- Table structure for table "contract"
--

DROP TABLE IF EXISTS "contract" CASCADE;
CREATE TABLE "contract"
(
    "id"                   varchar(36) NOT NULL UNIQUE,
    "account_id"           varchar(36) NOT NULL,
    "user_id"              varchar(36) NOT NULL,
    "created_time"         timestamp   NOT NULL,
    "signed_by_account_on" timestamp DEFAULT NULL,
    "signed_by_user_on"    timestamp DEFAULT NULL
);

-- --------------------------------------------------------

--
-- Table structure for table "loan"
--

DROP TABLE IF EXISTS "loan" CASCADE;
CREATE TABLE "loan"
(
    "id"          varchar(36) NOT NULL UNIQUE,
    "contract_id" varchar(36) NOT NULL,
    "return_date" date        NOT NULL
);

-- --------------------------------------------------------

--
-- Table structure for table "account"
--

DROP TABLE IF EXISTS "account" CASCADE;
CREATE TABLE "account"
(
    "id"           varchar(36) NOT NULL UNIQUE,
    "user_id"      varchar(36) NOT NULL,
    "username"     varchar(36) NOT NULL,
    "passwordHash" text        NOT NULL,
    "salt"         text        NOT NULL,
    "role"         text        NOT NULL,
    "active"       boolean     NOT NULL
);

-- --------------------------------------------------------

--
-- Table structure for table "reservation"
--

DROP TABLE IF EXISTS "reservation" CASCADE;
CREATE TABLE "reservation"
(
    "id"           varchar(36) NOT NULL UNIQUE,
    "contract_id"  varchar(36) NOT NULL,
    "from_date"    date        NOT NULL,
    "to_date"      date        NOT NULL,
    "activated_on" timestamp DEFAULT NULL
);

-- --------------------------------------------------------

--
-- Table structure for table "reservationItem"
--

DROP TABLE IF EXISTS "reservationItem" CASCADE;
CREATE TABLE "reservationItem"
(
    "item_id"        varchar(36) NOT NULL UNIQUE,
    "reservation_id" varchar(36) NOT NULL,
    "inventory_id"   varchar(36) NOT NULL,
    "amount"         integer     NOT NULL
);

-- --------------------------------------------------------

-- Indexes for table "inventory"
--
ALTER TABLE "inventory"
    ADD PRIMARY KEY ("id");

--
-- Indexes for table "inventoryMutation"
--
ALTER TABLE "inventoryMutation"
    ADD PRIMARY KEY ("mutation_id"),
    ADD FOREIGN KEY ("inventory_id") REFERENCES "inventory" ("id") ON DELETE CASCADE,
    ADD FOREIGN KEY ("loan_id") REFERENCES "loan" ("id") ON DELETE RESTRICT;
;

--
-- Indexes for table "user"
--
ALTER TABLE "user"
    ADD PRIMARY KEY ("id");

--
-- Indexes for table "contract"
--
ALTER TABLE "contract"
    ADD PRIMARY KEY ("id"),
    ADD FOREIGN KEY ("account_id") REFERENCES "account" ("id") ON DELETE RESTRICT,
    ADD FOREIGN KEY ("user_id") REFERENCES "user" ("id") ON DELETE RESTRICT;

--
-- Indexes for table "loan"
--
ALTER TABLE "loan"
    ADD PRIMARY KEY ("id"),
    ADD FOREIGN KEY ("contract_id") REFERENCES "contract" ("id") ON DELETE RESTRICT;

--
-- Indexes for table "account"
--
ALTER TABLE "account"
    ADD PRIMARY KEY ("id"),
    ADD FOREIGN KEY ("user_id") REFERENCES "user" ("id") ON DELETE RESTRICT;

--
-- Indexes for table "reservation"
--
ALTER TABLE "reservation"
    ADD PRIMARY KEY ("id"),
    ADD FOREIGN KEY ("contract_id") REFERENCES "contract" ("id") ON DELETE RESTRICT;

--
-- Indexes for table "reservationItem"
--
ALTER TABLE "reservationItem"
    ADD PRIMARY KEY ("item_id"),
    ADD FOREIGN KEY ("reservation_id") REFERENCES "reservation" ("id") ON DELETE CASCADE,
    ADD FOREIGN KEY ("inventory_id") REFERENCES "inventory" ("id") ON DELETE CASCADE;

--
-- Setup Admin Account
-- TODO: