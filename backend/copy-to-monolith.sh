#!/bin/bash

# Script to copy all Java files from microservices to monolith and update package names

set -e

BASE_DIR="/home/user/FREE-SERICE-DESK/backend"
MONOLITH_DIR="$BASE_DIR/monolith-app/src/main/java/com/servicedesk/monolith"

echo "========================================="
echo "Copying microservices to monolith..."
echo "========================================="

# Function to copy and update package names
copy_service() {
    local service_name=$1
    local module_name=$2

    echo "Processing $service_name -> $module_name module..."

    local src_dir="$BASE_DIR/$service_name/src/main/java/com/servicedesk/$module_name"
    local dest_dir="$MONOLITH_DIR/$module_name"

    if [ -d "$src_dir" ]; then
        # Copy all Java files
        find "$src_dir" -name "*.java" -type f | while read -r file; do
            # Get relative path
            rel_path="${file#$src_dir/}"
            dest_file="$dest_dir/$rel_path"

            # Create directory if needed
            mkdir -p "$(dirname "$dest_file")"

            # Copy and update package name
            sed "s|package com\.servicedesk\.$module_name|package com.servicedesk.monolith.$module_name|g" "$file" > "$dest_file"
        done

        echo "✓ Copied $(find "$src_dir" -name "*.java" -type f | wc -l) files from $service_name"
    else
        echo "⚠ Source directory not found: $src_dir"
    fi
}

# Copy each microservice
copy_service "ticket-service" "ticket"
copy_service "channel-service" "channel"
copy_service "notification-service" "notification"
copy_service "knowledge-service" "knowledge"
copy_service "ai-service" "ai"
copy_service "analytics-service" "analytics"
copy_service "marketplace-service" "marketplace"

echo ""
echo "========================================="
echo "Updating imports and references..."
echo "========================================="

# Update all imports in monolith to point to new packages
find "$MONOLITH_DIR" -name "*.java" -type f -exec sed -i \
    -e 's|import com\.servicedesk\.ticket\.|import com.servicedesk.monolith.ticket.|g' \
    -e 's|import com\.servicedesk\.channel\.|import com.servicedesk.monolith.channel.|g' \
    -e 's|import com\.servicedesk\.notification\.|import com.servicedesk.monolith.notification.|g' \
    -e 's|import com\.servicedesk\.knowledge\.|import com.servicedesk.monolith.knowledge.|g' \
    -e 's|import com\.servicedesk\.ai\.|import com.servicedesk.monolith.ai.|g' \
    -e 's|import com\.servicedesk\.analytics\.|import com.servicedesk.monolith.analytics.|g' \
    -e 's|import com\.servicedesk\.marketplace\.|import com.servicedesk.monolith.marketplace.|g' \
    {} \;

echo "✓ Updated imports in all files"

echo ""
echo "========================================="
echo "Summary"
echo "========================================="
echo "Total files copied: $(find "$MONOLITH_DIR" -name "*.java" -type f | wc -l)"
echo ""
echo "Module breakdown:"
for module in ticket channel notification knowledge ai analytics marketplace; do
    count=$(find "$MONOLITH_DIR/$module" -name "*.java" -type f 2>/dev/null | wc -l)
    echo "  $module: $count files"
done

echo ""
echo "Done! ✅"
