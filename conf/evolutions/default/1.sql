# --- !Ups

CREATE TABLE "category"
(
    "id"   INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "name" VARCHAR NOT NULL
);

CREATE TABLE "product"
(
    "id"          INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "name"        VARCHAR NOT NULL,
    "description" VARCHAR NOT NULL,
    "category"    INT     NOT NULL,
    "price"       INT     NOT NULL,
    FOREIGN KEY (category) references category (id)
);

CREATE TABLE "comment"
(
    "id"      INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "product" INT NOT NULL,
    "comment" VARCHAR NOT NULL,
    FOREIGN KEY (product) references product (id)
);

CREATE TABLE "rate"
(
    "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "product" INTEGER NOT NULL,
    "grade" INTEGER NOT NULL,
    FOREIGN KEY (product) references product (id)

);

CREATE TABLE "cart"
(
    "id"   INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "user" INT     NOT NULL,
    "product" INT     NOT NULL,
    FOREIGN KEY (user) references user (id),
    FOREIGN KEY (product) references product (id)
);

CREATE TABLE "voucher"
(
    "id"    INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "name"  VARCHAR NOT NULL,
    "value" INT     NOT NULL
);

CREATE TABLE "order"
(
    "id"    INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "user"  INT     NOT NULL,
    "price" INT     NOT NULL,
    FOREIGN KEY (user) references user (id)
);

CREATE TABLE "shipping"
(
   "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
   "user"  INT     NOT NULL,
   "address" VARCHAR NOT NULL,
   "typeOf" INTEGER NOT NULL,
   FOREIGN KEY (user) references user (id)
);

CREATE TABLE "invoice" (
    "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "order" INTEGER NOT NULL
);

CREATE TABLE "return"
(
    "id"   INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "user" INT     NOT NULL,
    "order" INT     NOT NULL,
    FOREIGN KEY (user) references user (id)
);

CREATE TABLE "user"
(
    "id"          INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "providerId"  VARCHAR NOT NULL,
    "providerKey" VARCHAR NOT NULL,
    "email"       VARCHAR NOT NULL,
    "firstName"   VARCHAR NOT NULL,
    "lastName"    VARCHAR NOT NULL,
    "login"       VARCHAR NOT NULL
);

CREATE TABLE "authToken"
(
    "id"     INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "userId" INT     NOT NULL,
    FOREIGN KEY (userId) references user (id)
);

CREATE TABLE "passwordInfo"
(
    "id"          INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "providerId"  VARCHAR NOT NULL,
    "providerKey" VARCHAR NOT NULL,
    "hasher"      VARCHAR NOT NULL,
    "password"    VARCHAR NOT NULL,
    "salt"        VARCHAR
);

CREATE TABLE "oAuth2Info"
(
    "id"          INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "providerId"  VARCHAR NOT NULL,
    "providerKey" VARCHAR NOT NULL,
    "accessToken" VARCHAR NOT NULL,
    "tokenType"   VARCHAR,
    "expiresIn"   INTEGER
);

# --- !Downs
DROP TABLE "category";
DROP TABLE "product";
DROP TABLE "comment";
DROP TABLE "rate";
DROP TABLE "cart";
DROP TABLE "voucher";
DROP TABLE "order";
DROP TABLE "shipping";
DROP TABLE "invoice";
DROP TABLE "return";
DROP TABLE "user";
DROP TABLE "authToken";
DROP TABLE "passwordInfo";
DROP TABLE "oAuth2Info";
