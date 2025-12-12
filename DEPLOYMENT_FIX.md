# 🔍 Root Cause Analysis - Flyway Migration Checksum Mismatch

## What Happened

The V5 migration file (`V5__macro_presets.sql`) was **modified after it was already applied** to the production database on Railway.

### The Change

**Original version (applied to production):**
```sql
protein_percentage DECIMAL(5, 2) NOT NULL,
carbs_percentage DECIMAL(5, 2) NOT NULL,
fats_percentage DECIMAL(5, 2) NOT NULL,
```

**Current version (in codebase):**
```sql
protein_percentage INT NOT NULL,
carbs_percentage INT NOT NULL,
fats_percentage INT NOT NULL,
```

### Why This Breaks Deployment

1. Flyway calculates a **checksum** for each migration file
2. When the migration was first applied to production, it stored checksum `-1719866606`
3. After modifying the file, the new checksum became `508745468`
4. On deployment, Flyway compares checksums and **fails validation** because they don't match
5. This prevents the application from starting

---

## 🚨 Impact

- **Application Status**: Cannot start on Railway
- **Database Status**: Has the OLD schema (DECIMAL columns)
- **Code Status**: Expects INT columns
- **Risk Level**: HIGH - Schema mismatch between database and application

---

## ✅ Complete Fix Strategy

You need to fix **BOTH** the Flyway checksum AND the database schema.

### Step 1: Fix the Database Schema

The production database currently has `DECIMAL(5,2)` columns but your code expects `INT` columns. You need to create a new migration to alter the table.

**Create file:** `src/main/resources/db/migration/V7__alter_macro_presets_percentages.sql`

```sql
-- Alter macro_presets table to change percentage columns from DECIMAL to INT
ALTER TABLE macro_presets 
    MODIFY COLUMN protein_percentage INT NOT NULL,
    MODIFY COLUMN carbs_percentage INT NOT NULL,
    MODIFY COLUMN fats_percentage INT NOT NULL;
```

### Step 2: Fix the Flyway Checksum

You have **two options**:

#### Option A: Revert V5 to Original (RECOMMENDED)

1. **Revert the V5 file to its original state:**

```bash
git show 59068a9:src/main/resources/db/migration/V5__macro_presets.sql > src/main/resources/db/migration/V5__macro_presets.sql
```

2. **Commit the revert:**

```bash
git add src/main/resources/db/migration/V5__macro_presets.sql
git commit -m "revert: Restore V5 migration to original state"
```

3. **The V7 migration (from Step 1) will handle the schema change**

#### Option B: Update Database Checksum (NOT RECOMMENDED)

Connect to your Railway MySQL database and run:

```sql
UPDATE flyway_schema_history 
SET checksum = 508745468 
WHERE version = '5';
```

⚠️ **Warning**: This approach is risky because your database schema won't match what V5 claims to have created.

---

## 📋 Complete Action Plan

### Immediate Fix (Use Option A)

1. **Create the V7 migration file** (see Step 1 above)

2. **Revert V5 to original:**
   ```bash
   cd c:\Users\ikerg\.gemini\antigravity\scratch\nutrition-tracker-api
   git show 59068a9:src/main/resources/db/migration/V5__macro_presets.sql > src/main/resources/db/migration/V5__macro_presets.sql
   ```

3. **Commit both changes:**
   ```bash
   git add src/main/resources/db/migration/
   git commit -m "fix: Revert V5 migration and add V7 to alter percentage columns to INT"
   git push origin main
   ```

4. **Redeploy on Railway** - The deployment should now succeed:
   - Flyway will validate V5 ✅ (checksums match)
   - Flyway will run V7 ✅ (alters columns to INT)
   - Application will start ✅

---

## 🛡️ Prevention for Future

### Golden Rules for Flyway Migrations

1. **NEVER modify a migration file after it's been applied to ANY environment**
2. **ALWAYS create a new migration file** for schema changes
3. **Use version control** to track migration history
4. **Test migrations** in a staging environment before production

### Recommended Workflow

```
Development → Staging → Production
     ↓           ↓          ↓
  Create V7   Test V7   Apply V7
```

### Add Git Hook (Optional)

Create `.git/hooks/pre-commit`:

```bash
#!/bin/bash
# Prevent modification of applied migrations
git diff --cached --name-only | grep "db/migration/V[1-6]__" && {
    echo "ERROR: Cannot modify applied migration files!"
    exit 1
}
exit 0
```

---

## 🔍 Verification Steps

After deploying the fix:

1. **Check Flyway schema history:**
   ```sql
   SELECT version, description, checksum, success 
   FROM flyway_schema_history 
   ORDER BY installed_rank;
   ```

2. **Verify table schema:**
   ```sql
   DESCRIBE macro_presets;
   ```
   
   Should show:
   ```
   protein_percentage | int | NO
   carbs_percentage   | int | NO
   fats_percentage    | int | NO
   ```

3. **Check application logs** - Should start without errors

---

## 📞 Need Help?

If you encounter any issues:

1. Check Railway logs for specific error messages
2. Verify database connection settings
3. Ensure all environment variables are set correctly
4. Check that V7 migration file is in the correct location

---

## Summary

- **Problem**: V5 migration was modified after being applied to production
- **Impact**: Application cannot start due to checksum mismatch
- **Solution**: Revert V5 to original + Create V7 to alter schema
- **Prevention**: Never modify applied migrations, always create new ones
