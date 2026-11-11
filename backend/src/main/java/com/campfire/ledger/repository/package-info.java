/**
 * Domain-facing data access. Composes DAO calls into what services need (for example posted balances per account). Services depend on this, never on DAOs or SQL.
 */
package com.campfire.ledger.repository;
