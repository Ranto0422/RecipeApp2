<?php
header('Content-Type: application/json');
require 'db.php';

$data = json_decode(file_get_contents('php://input'), true);
error_log('Register data: ' . json_encode($data));
$name = $data['name'] ?? '';
$email = $data['email'] ?? '';
$password = $data['password'] ?? '';
$role = $data['role'] ?? 'user';

if (empty($name) || empty($email) || empty($password)) {
    echo json_encode(['success' => false, 'message' => 'Missing fields']);
    exit;
}

$hashed = password_hash($password, PASSWORD_DEFAULT);

try {
    $stmt = $db->prepare('INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, ?)');
    $stmt->execute([$name, $email, $hashed, $role]);
    $userId = $db->lastInsertId();
    echo json_encode(['success' => true, 'message' => 'User registered successfully', 'userId' => $userId]);
} catch (PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'User registration failed: ' . $e->getMessage()]);
}
?>
