<?php
header('Content-Type: application/json');
require 'db.php';

// Fetch all public recipes that are not yet approved
$stmt = $db->prepare('SELECT * FROM recipes WHERE visibility = "Public" AND isApproved = 0');
$stmt->execute();
$recipes = $stmt->fetchAll(PDO::FETCH_ASSOC);

// Ensure all fields are present and JSON compatible
foreach ($recipes as &$recipe) {
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

echo json_encode(['success' => true, 'recipes' => $recipes]);
?>
