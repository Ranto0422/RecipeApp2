<?php
header('Content-Type: application/json');
require 'db.php';

// Required fields
$required = ['name', 'ingredients', 'instructions', 'servings', 'tags', 'image', 'cookTimeMinutes'];
$errors = [];
foreach ($required as $field) {
    if (empty($_POST[$field])) {
        $errors[] = $field . ' is required.';
    }
}
if (!empty($errors)) {
    http_response_code(400);
    echo json_encode(['success' => false, 'errors' => $errors]);
    exit();
}

// Get required fields
$name = $_POST['name'];
$ingredients = $_POST['ingredients'];
$instructions = $_POST['instructions'];
$servings = $_POST['servings'];
$tags = $_POST['tags'];
$image = $_POST['image'];
$cookTimeMinutes = $_POST['cookTimeMinutes'];

// Optional fields
$prepTimeMinutes = isset($_POST['prepTimeMinutes']) ? $_POST['prepTimeMinutes'] : null;
$cuisine = isset($_POST['cuisine']) ? $_POST['cuisine'] : null;
$difficulty = isset($_POST['difficulty']) ? $_POST['difficulty'] : null;
$userId = isset($_POST['userId']) ? $_POST['userId'] : null;
$visibility = isset($_POST['visibility']) ? $_POST['visibility'] : 'Only me';
// Normalize visibility value for comparison
$normalizedVisibility = strtolower(trim($visibility));
$isApproved = ($normalizedVisibility === 'public') ? 0 : 1;

// Validate userId
if (empty($userId)) {
    http_response_code(400);
    echo json_encode(['success' => false, 'error' => 'Missing userId. Please log in and try again.']);
    exit();
}

// Save to database
$sql = "INSERT INTO recipes (name, ingredients, instructions, servings, tags, image, cookTimeMinutes, prepTimeMinutes, cuisine, difficulty, userId, visibility, isApproved)
        VALUES (:name, :ingredients, :instructions, :servings, :tags, :image, :cookTimeMinutes, :prepTimeMinutes, :cuisine, :difficulty, :userId, :visibility, :isApproved)";
$stmt = $db->prepare($sql);
try {
    $stmt->execute([
        ':name' => $name,
        ':ingredients' => $ingredients,
        ':instructions' => $instructions,
        ':servings' => $servings,
        ':tags' => $tags,
        ':image' => $image,
        ':cookTimeMinutes' => $cookTimeMinutes,
        ':prepTimeMinutes' => $prepTimeMinutes,
        ':cuisine' => $cuisine,
        ':difficulty' => $difficulty,
        ':userId' => $userId,
        ':visibility' => $visibility,
        ':isApproved' => $isApproved
    ]);
    echo json_encode(['success' => true, 'message' => 'Recipe added successfully.']);
} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode(['success' => false, 'error' => 'Failed to add recipe: ' . $e->getMessage()]);
    exit();
}
?>
