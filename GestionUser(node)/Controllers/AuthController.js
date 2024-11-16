// controllers/UserController.js
const UserService = require('../Services/AuthService');

class UserController {


    // Méthode pour enregistrer un utilisateur
    async register(req, res) {
        try {
            const user = await UserService.registerUser(req.body);
            return res.status(201).json({
                message: 'User registered successfully',
                user,
            });
        } catch (error) {
            console.error(error);
            return res.status(500).json({ message: 'Error registering user' });
        }
    }




     // Méthode pour le login
  // controllers/AuthController.js
async login(req, res) {
    try {
        const { email, password } = req.body;
        const { user, token } = await UserService.loginUser(email, password);
        
        // Stocker le token dans l'en-tête de réponse
        res.setHeader('Authorization', `Bearer ${token}`);
        
        return res.status(200).json({ user }); // Inclure seulement l'utilisateur dans la réponse
    } catch (error) {
        return res.status(401).json({ message: 'Invalid email or password' });
    }
}

    





 // Méthode pour obtenir tous les utilisateurs
 async getAllUsers(req, res) {
    try {
        const users = await UserService.getAllUsers(); // Correction ici: UserService
        return res.status(200).json(users);
    } catch (error) {
        return res.status(500).json({ message: "Error fetching users", error: error.message });
    }
}

// Méthode pour obtenir un utilisateur par ID
async getUserById(req, res) {
    try {
        const user = await UserService.getUserById(req.params.id); // Correction ici: UserService
        if (!user) {
            return res.status(404).json({ message: "User not found" });
        }
        return res.status(200).json(user);
    } catch (error) {
        return res.status(500).json({ message: "Error fetching user", error: error.message });
    }
}

// Méthode pour mettre à jour un utilisateur
async updateUser(req, res) {
    try {
        const user = await UserService.updateUser(req.params.id, req.body); // Correction ici: UserService
        if (!user) {
            return res.status(404).json({ message: "User not found" });
        }
        return res.status(200).json({ message: "User updated successfully", user });
    } catch (error) {
        return res.status(500).json({ message: "Error updating user", error: error.message });
    }
}

// Méthode pour supprimer un utilisateur
async deleteUser(req, res) {
    try {
        const user = await UserService.deleteUser(req.params.id); // Correction ici: UserService
        if (!user) {
            return res.status(404).json({ message: "User not found" });
        }
        return res.status(200).json({ message: "User deleted successfully" });
    } catch (error) {
        return res.status(500).json({ message: "Error deleting user", error: error.message });
    }
}
}

module.exports = new UserController();