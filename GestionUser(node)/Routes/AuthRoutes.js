const express = require('express');
const AuthController = require('../controllers/AuthController');
const authMiddleware = require('../Middleware/middleware'); // Chemin vers votre middleware

const router = express.Router();

// Route d'inscription
router.post('/register', AuthController.register);

// Route de connexion
router.post('/login', AuthController.login);

// Route pour récupérer tous les utilisateurs
router.get('/',authMiddleware, AuthController.getAllUsers);

// Route pour récupérer un utilisateur par ID
router.get('/:id', AuthController.getUserById);

// Route pour mettre à jour un utilisateur
router.put('/:id', AuthController.updateUser);

// Route pour supprimer un utilisateur
router.delete('/:id', AuthController.deleteUser);
module.exports = router;
