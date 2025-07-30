<?php
// Set upload directory
$target_dir = "C:/xampp/htdocs/MyRecipeUploads/";

// Check if file was sent
if (!isset($_FILES["image"])) {
    http_response_code(400);
    echo json_encode(["error" => "No file uploaded."]);
    exit();
}

$file = $_FILES["image"];
$filename = basename($file["name"]);
$target_file = $target_dir . $filename;

// Check file type makes sures that the file is an imafe
$imageFileType = strtolower(pathinfo($target_file, PATHINFO_EXTENSION));
$allowed = ["jpg", "jpeg", "png", "gif"];
if (!in_array($imageFileType, $allowed)) {
    http_response_code(400);
    echo json_encode(["error" => "Only image files are allowed."]);
    exit();
}

// Moving the uploaded file
if (move_uploaded_file($file["tmp_name"], $target_file)) {
    // Return the URL to access the image
    $url = "http://localhost/MyRecipeUploads/" . urlencode($filename);
    echo json_encode(["success" => true, "url" => $url]);
} else {
    http_response_code(500);
    echo json_encode(["error" => "Failed to upload file."]);
}
?>

