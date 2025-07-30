<?php
header('Content-Type: application/json');
require 'db.php';

// Required fields
$required = ['recipeId', 'name', 'ingredients', 'instructions', 'servings', 'tags', 'cookTimeMinutes'];
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
$recipeId = $_POST['recipeId'];
$name = $_POST['name'];
$ingredients = $_POST['ingredients'];
$instructions = $_POST['instructions'];
$servings = $_POST['servings'];
$tags = $_POST['tags'];
$cookTimeMinutes = $_POST['cookTimeMinutes'];

// Optional fields
$prepTimeMinutes = isset($_POST['prepTimeMinutes']) ? $_POST['prepTimeMinutes'] : null;
$cuisine = isset($_POST['cuisine']) ? $_POST['cuisine'] : null;
$difficulty = isset($_POST['difficulty']) ? $_POST['difficulty'] : null;
$visibility = isset($_POST['visibility']) ? $_POST['visibility'] : 'Only me';
$image = isset($_POST['image']) ? $_POST['image'] : null;

// If recipe was approved and is being edited, reset approval status
$normalizedVisibility = strtolower(trim($visibility));
$isApproved = ($normalizedVisibility === 'public') ? 0 : 1;

// Update recipe in database
$sql = "UPDATE recipes SET
        name = :name,
        ingredients = :ingredients,
        instructions = :instructions,
        servings = :servings,
        tags = :tags,
        cookTimeMinutes = :cookTimeMinutes,
        prepTimeMinutes = :prepTimeMinutes,
        cuisine = :cuisine,
        difficulty = :difficulty,
        visibility = :visibility,
        isApproved = :isApproved";

// Only update image if provided
if ($image !== null) {
    $sql .= ", image = :image";
}

$sql .= " WHERE id = :recipeId";

$stmt = $db->prepare($sql);

$params = [
    ':recipeId' => $recipeId,
    ':name' => $name,
    ':ingredients' => $ingredients,
    ':instructions' => $instructions,
    ':servings' => $servings,
    ':tags' => $tags,
    ':cookTimeMinutes' => $cookTimeMinutes,
    ':prepTimeMinutes' => $prepTimeMinutes,
    ':cuisine' => $cuisine,
    ':difficulty' => $difficulty,
    ':visibility' => $visibility,
    ':isApproved' => $isApproved
];

if ($image !== null) {
    $params[':image'] = $image;
}

try {
    $stmt->execute($params);

    if ($stmt->rowCount() > 0) {
        echo json_encode(['success' => true, 'message' => 'Recipe updated successfully.']);
    } else {
        http_response_code(404);
        echo json_encode(['success' => false, 'error' => 'Recipe not found or no changes made.']);
    }
} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode(['success' => false, 'error' => 'Failed to update recipe: ' . $e->getMessage()]);
}
?>
