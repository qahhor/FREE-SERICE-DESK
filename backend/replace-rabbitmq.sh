#!/bin/bash

# Script to replace RabbitMQ with Spring Events in monolith

set -e

MONOLITH_DIR="/home/user/FREE-SERICE-DESK/backend/monolith-app/src/main/java/com/servicedesk/monolith"

echo "========================================="
echo "Replacing RabbitMQ with Spring Events..."
echo "========================================="

# Remove RabbitMQ imports
find "$MONOLITH_DIR" -name "*.java" -type f -exec sed -i \
    -e '/import org\.springframework\.amqp/d' \
    -e '/import org\.springframework\.rabbit/d' \
    -e '/@RabbitListener/d' \
    -e '/@RabbitHandler/d' \
    {} \;

echo "✓ Removed RabbitMQ imports and annotations"

# Replace RabbitTemplate with ApplicationEventPublisher
find "$MONOLITH_DIR" -name "*.java" -type f -exec sed -i \
    -e 's/RabbitTemplate/ApplicationEventPublisher/g' \
    -e 's/rabbitTemplate/eventPublisher/g' \
    {} \;

echo "✓ Replaced RabbitTemplate with ApplicationEventPublisher"

# Add ApplicationEventPublisher import where needed
find "$MONOLITH_DIR" -name "*.java" -type f -exec grep -l "ApplicationEventPublisher" {} \; | while read -r file; do
    if ! grep -q "import org.springframework.context.ApplicationEventPublisher" "$file"; then
        # Add import after package declaration
        sed -i '/^package /a\\\nimport org.springframework.context.ApplicationEventPublisher;' "$file"
    fi
done

echo "✓ Added ApplicationEventPublisher imports"

echo ""
echo "Done! ✅"
echo ""
echo "Note: You will need to manually update:"
echo "1. RabbitTemplate.convertAndSend() calls to eventPublisher.publishEvent()"
echo "2. @RabbitListener methods to @EventListener methods"
echo "3. Method signatures to accept Event objects instead of raw data"
