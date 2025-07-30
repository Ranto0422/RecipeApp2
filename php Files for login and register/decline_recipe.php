<?php
header('Content-Type: application/json');
require 'db.php';

function safe_json_response($arr) {
    if (!isset($arr['error'])) {
        $arr['error'] = "";
    } elseif (!is_string($arr['error']) || $arr['error'] === null) {
        $arr['error'] = is_null($arr['error']) ? '' : strval($arr['error']);
    }
    echo json_encode($arr);
}

$recipeId = $_POST['recipeId'] ?? null;
if (!$recipeId) {
    http_response_code(400);
    $error = 'Missing recipeId';
    safe_json_response(['success' => false, 'error' => $error]);
    error_log('decline_recipe.php response: ' . json_encode(['success' => false, 'error' => $error]));
    exit();
}

// Mark recipe as declined (isApproved = -1)
$stmt = $db->prepare('UPDATE recipes SET isApproved = -1 WHERE id = ?');
try {
    $stmt->execute([$recipeId]);
    // Check if update was successful
    if ($stmt->rowCount() > 0) {
        $response = ['success' => true, 'message' => 'Recipe declined.', 'error' => ''];
    } else {
        http_response_code(404);
        $error = 'Recipe not found or already declined.';
        $response = ['success' => false, 'error' => $error];
    }
    safe_json_response($response);
    error_log('decline_recipe.php response: ' . json_encode($response));
} catch (PDOException $e) {
    http_response_code(500);
    $error = 'Failed to decline recipe: ' . $e->getMessage();
    $response = ['success' => false, 'error' => $error];
    safe_json_response($response);
    error_log('decline_recipe.php response: ' . json_encode($response));
    exit();
}
?>