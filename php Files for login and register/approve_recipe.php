<?php
header('Content-Type: application/json');
require 'db.php';

function safe_json_response($arr) {
    $arr['error'] = isset($arr['error']) && is_string($arr['error']) ? $arr['error'] : '';
    echo json_encode($arr);
}

$recipeId = $_POST['recipeId'] ?? null;
if (!$recipeId) {
    http_response_code(400);
    $error = 'Missing recipeId';
    safe_json_response(['success' => false, 'error' => $error]);
    error_log('approve_recipe.php response: ' . json_encode(['success' => false, 'error' => $error]));
    exit();
}

$stmt = $db->prepare('UPDATE recipes SET isApproved = 1 WHERE id = ?');
try {
    $stmt->execute([$recipeId]);
    if ($stmt->rowCount() > 0) {
        $response = ['success' => true, 'message' => 'Recipe approved.', 'error' => ''];
    } else {
        http_response_code(404);
        $error = 'Recipe not found or already approved.';
        $response = ['success' => false, 'error' => $error];
    }
    safe_json_response($response);
    error_log('approve_recipe.php response: ' . json_encode($response));
} catch (PDOException $e) {
    http_response_code(500);
    $error = 'Failed to approve recipe: ' . $e->getMessage();
    $response = ['success' => false, 'error' => $error];
    safe_json_response($response);
    error_log('approve_recipe.php response: ' . json_encode($response));
    exit();
}
?>