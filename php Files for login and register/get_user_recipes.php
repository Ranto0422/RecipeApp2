<?php
header('Content-Type: application/json');
require __DIR__ . '/db.php'; // Correct path

$userId = $_GET['userId'] ?? null;
if (!$userId) {
    http_response_code(400);
    echo json_encode(['success' => false, 'error' => 'Missing userId']);
    exit();
}
// Cast userId to int for query
$userId = (int)$userId;
$stmt = $db->prepare("SELECT * FROM recipes WHERE userId = ?");
$stmt->execute([$userId]);
$recipes = $stmt->fetchAll(PDO::FETCH_ASSOC);

// Debug: log all recipes for this userId
error_log('get_user_recipes.php: userId=' . $userId);
error_log('get_user_recipes.php: found recipes=' . json_encode($recipes));

// Ensure all fields are present and JSON compatible
foreach ($recipes as &$recipe) {
    // Convert any nulls to empty strings for JSON compatibility
    foreach ($recipe as $key => $value) {
        if (is_null($value)) {
            $recipe[$key] = "";
        }
    }
    // Decode ingredients and instructions if they are JSON strings
    if (!empty($recipe['ingredients']) && is_string($recipe['ingredients'])) {
        $decoded = json_decode($recipe['ingredients'], true);
        if (json_last_error() === JSON_ERROR_NONE) {
            $recipe['ingredients'] = $decoded;
        }
    }
    if (!empty($recipe['instructions']) && is_string($recipe['instructions'])) {
        $decoded = json_decode($recipe['instructions'], true);
        if (json_last_error() === JSON_ERROR_NONE) {
            $recipe['instructions'] = $decoded;
        }
    }
}

// Return recipes as JSON
echo json_encode(['success' => true, 'recipes' => $recipes]);
