#!/bin/bash

# Exit immediately if a command exits with a non-zero status.
set -e

CONFIG_FILE=".db_copy_env"

# --- Load Existing Configuration ---
if [ -f "$CONFIG_FILE" ]; then
    read -p "A saved configuration was found. Would you like to load it? (y/n): " use_config
    if [[ "$use_config" == "y" || "$use_config" == "Y" ]]; then
        echo "--> Loading configuration from '$CONFIG_FILE'..."
        source "$CONFIG_FILE"
        CONFIG_LOADED=true
    fi
fi

echo ""

# --- Interactive Setup (if config not loaded) ---
if [ -z "$CONFIG_LOADED" ]; then
    echo "--- Setting Up Local Database Details ---"
    CURRENT_USER=$(whoami)
    read -p "Enter local username [$CURRENT_USER]: " LOCAL_USER
    LOCAL_USER=${LOCAL_USER:-$CURRENT_USER}

    read -p "Enter local database name [$LOCAL_USER]: " LOCAL_DB
    LOCAL_DB=${LOCAL_DB:-$LOCAL_USER}

    echo ""
    echo "--- Setting Up Remote Database Details ---"
    read -p "Enter remote host: " REMOTE_HOST
    read -p "Enter remote username [$LOCAL_USER]: " REMOTE_USER
    REMOTE_USER=${REMOTE_USER:-$LOCAL_USER}

    read -p "Enter remote database name [$LOCAL_DB]: " REMOTE_DB
    REMOTE_DB=${REMOTE_DB:-$LOCAL_DB}

    echo ""
    read -p "Would you like to save these settings for next time? (y/n): " save_config
    if [[ "$save_config" == "y" || "$save_config" == "Y" ]]; then
        echo "--> Saving configuration to '$CONFIG_FILE'..."
        {
            echo "LOCAL_USER='$LOCAL_USER'"
            echo "LOCAL_DB='$LOCAL_DB'"
            echo "REMOTE_HOST='$REMOTE_HOST'"
            echo "REMOTE_USER='$REMOTE_USER'"
            echo "REMOTE_DB='$REMOTE_DB'"
        } > "$CONFIG_FILE"
    fi
fi

echo ""

# --- Script Logic ---

# Prompt securely for the remote password 🔒
echo -n "Enter password for remote user '${REMOTE_USER}': "
read -s REMOTE_PASSWORD
echo

export PGPASSWORD=$REMOTE_PASSWORD

# 1. Backup the local database 🛡️
TIMESTAMP=$(date +%F_%T)
BACKUP_FILE="${LOCAL_DB}_${TIMESTAMP}.dump"

echo "--> Backing up local database '${LOCAL_DB}' to '${BACKUP_FILE}'..."
pg_dump -h localhost -U "$LOCAL_USER" -d "$LOCAL_DB" -F c -b -v -f "$BACKUP_FILE"
echo "--> Backup successful."

# 2. Create the remote database 🏗️
echo "--> Creating remote database '${REMOTE_DB}' on host '${REMOTE_HOST}'..."
createdb -U "$REMOTE_USER" -h "$REMOTE_HOST" -T template0 "$REMOTE_DB" || echo "Database '${REMOTE_DB}' already exists or another error occurred. Proceeding with restore."

# 3. Restore the backup to the remote database 🚀
echo "--> Restoring backup to remote database '${REMOTE_DB}'..."
pg_restore --verbose --clean --no-acl --no-owner \
  -U "$REMOTE_USER" \
  -h "$REMOTE_HOST" \
  -d "$REMOTE_DB" \
  "$BACKUP_FILE"

unset PGPASSWORD

echo ""
echo "✅ Database copy complete! '${LOCAL_DB}' has been copied to '${REMOTE_DB}' on '${REMOTE_HOST}'."
echo "Backup file is located at: $(pwd)/${BACKUP_FILE}"