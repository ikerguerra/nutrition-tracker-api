# React Integration Guide - Nutrition Tracker API

## 📋 Tabla de Contenidos

1. [Configuración Inicial](#configuración-inicial)
2. [Servicio API](#servicio-api)
3. [Hooks Personalizados](#hooks-personalizados)
4. [Componentes de Ejemplo](#componentes-de-ejemplo)
5. [Manejo de Errores](#manejo-de-errores)
6. [TypeScript Definitions](#typescript-definitions)

---

## 🚀 Configuración Inicial

### 1. Instalar Dependencias

```bash
npm install axios
# o
yarn add axios
```

### 2. Variables de Entorno

Crea un archivo `.env` en la raíz de tu proyecto React:

```env
REACT_APP_API_BASE_URL=http://localhost:8080/api/v1
```

Para Vite:
```env
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

---

## 🔌 Servicio API

### Configuración Base de Axios

Crea `src/services/api.js`:

```javascript
import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api/v1';

// Crear instancia de axios con configuración base
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000, // 10 segundos
});

// Interceptor para requests (útil para añadir tokens en el futuro)
api.interceptors.request.use(
  (config) => {
    // Aquí podrás añadir el token JWT cuando implementes autenticación
    // const token = localStorage.getItem('token');
    // if (token) {
    //   config.headers.Authorization = `Bearer ${token}`;
    // }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Interceptor para responses (manejo de errores global)
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      // El servidor respondió con un código de error
      console.error('Error Response:', error.response.data);
    } else if (error.request) {
      // La petición se hizo pero no hubo respuesta
      console.error('No Response:', error.request);
    } else {
      // Error al configurar la petición
      console.error('Error:', error.message);
    }
    return Promise.reject(error);
  }
);

export default api;
```

### Servicio de Alimentos

Crea `src/services/foodService.js`:

```javascript
import api from './api';

const foodService = {
  /**
   * Crear un nuevo alimento
   * @param {Object} foodData - Datos del alimento
   * @returns {Promise} Alimento creado
   */
  createFood: async (foodData) => {
    try {
      const response = await api.post('/foods', foodData);
      return response.data.data; // Extrae el 'data' del ApiResponse
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  /**
   * Obtener alimento por ID
   * @param {number} id - ID del alimento
   * @returns {Promise} Alimento encontrado
   */
  getFoodById: async (id) => {
    try {
      const response = await api.get(`/foods/${id}`);
      return response.data.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  /**
   * Obtener todos los alimentos con paginación
   * @param {Object} params - Parámetros de paginación
   * @param {number} params.page - Número de página (0-indexed)
   * @param {number} params.size - Tamaño de página
   * @param {string} params.sortBy - Campo para ordenar
   * @param {string} params.direction - Dirección (asc/desc)
   * @returns {Promise} Página de alimentos
   */
  getAllFoods: async ({ page = 0, size = 20, sortBy = 'name', direction = 'asc' } = {}) => {
    try {
      const response = await api.get('/foods', {
        params: { page, size, sortBy, direction }
      });
      return response.data.data; // Retorna objeto Page con content, totalElements, etc.
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  /**
   * Buscar alimentos por nombre o marca
   * @param {string} query - Texto de búsqueda
   * @param {Object} params - Parámetros de paginación
   * @returns {Promise} Resultados de búsqueda
   */
  searchFoods: async (query, { page = 0, size = 20 } = {}) => {
    try {
      const response = await api.get('/foods/search', {
        params: { query, page, size }
      });
      return response.data.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  /**
   * Buscar alimento por código de barras
   * @param {string} barcode - Código de barras
   * @returns {Promise} Resultado de búsqueda con información de origen
   */
  searchByBarcode: async (barcode) => {
    try {
      const response = await api.get(`/foods/barcode/${barcode}`);
      return response.data.data; // Retorna BarcodeSearchResponseDto
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  /**
   * Actualizar un alimento existente
   * @param {number} id - ID del alimento
   * @param {Object} foodData - Datos actualizados
   * @returns {Promise} Alimento actualizado
   */
  updateFood: async (id, foodData) => {
    try {
      const response = await api.put(`/foods/${id}`, foodData);
      return response.data.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  /**
   * Eliminar un alimento
   * @param {number} id - ID del alimento
   * @returns {Promise} void
   */
  deleteFood: async (id) => {
    try {
      await api.delete(`/foods/${id}`);
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  /**
   * Obtener el total de alimentos
   * @returns {Promise<number>} Total de alimentos
   */
  getTotalCount: async () => {
    try {
      const response = await api.get('/foods/stats/count');
      return response.data.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  }
};

export default foodService;
```

---

## 🎣 Hooks Personalizados

### Hook para Gestión de Alimentos

Crea `src/hooks/useFoods.js`:

```javascript
import { useState, useEffect, useCallback } from 'react';
import foodService from '../services/foodService';

export const useFoods = (initialPage = 0, initialSize = 20) => {
  const [foods, setFoods] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [pagination, setPagination] = useState({
    page: initialPage,
    size: initialSize,
    totalElements: 0,
    totalPages: 0,
  });

  // Cargar alimentos
  const loadFoods = useCallback(async (page = pagination.page, size = pagination.size) => {
    setLoading(true);
    setError(null);
    try {
      const response = await foodService.getAllFoods({ page, size });
      setFoods(response.content);
      setPagination({
        page: response.number,
        size: response.size,
        totalElements: response.totalElements,
        totalPages: response.totalPages,
      });
    } catch (err) {
      setError(err.message || 'Error al cargar alimentos');
    } finally {
      setLoading(false);
    }
  }, [pagination.page, pagination.size]);

  // Crear alimento
  const createFood = async (foodData) => {
    setLoading(true);
    setError(null);
    try {
      const newFood = await foodService.createFood(foodData);
      await loadFoods(); // Recargar lista
      return newFood;
    } catch (err) {
      setError(err.message || 'Error al crear alimento');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  // Actualizar alimento
  const updateFood = async (id, foodData) => {
    setLoading(true);
    setError(null);
    try {
      const updatedFood = await foodService.updateFood(id, foodData);
      await loadFoods(); // Recargar lista
      return updatedFood;
    } catch (err) {
      setError(err.message || 'Error al actualizar alimento');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  // Eliminar alimento
  const deleteFood = async (id) => {
    setLoading(true);
    setError(null);
    try {
      await foodService.deleteFood(id);
      await loadFoods(); // Recargar lista
    } catch (err) {
      setError(err.message || 'Error al eliminar alimento');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  // Buscar alimentos
  const searchFoods = async (query) => {
    setLoading(true);
    setError(null);
    try {
      const response = await foodService.searchFoods(query);
      setFoods(response.content);
      setPagination({
        page: response.number,
        size: response.size,
        totalElements: response.totalElements,
        totalPages: response.totalPages,
      });
    } catch (err) {
      setError(err.message || 'Error al buscar alimentos');
    } finally {
      setLoading(false);
    }
  };

  // Cambiar página
  const changePage = (newPage) => {
    loadFoods(newPage, pagination.size);
  };

  // Cargar al montar
  useEffect(() => {
    loadFoods();
  }, []);

  return {
    foods,
    loading,
    error,
    pagination,
    createFood,
    updateFood,
    deleteFood,
    searchFoods,
    changePage,
    refresh: loadFoods,
  };
};
```

### Hook para Búsqueda por Código de Barras

Crea `src/hooks/useBarcodeSearch.js`:

```javascript
import { useState } from 'react';
import foodService from '../services/foodService';

export const useBarcodeSearch = () => {
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const searchByBarcode = async (barcode) => {
    setLoading(true);
    setError(null);
    setResult(null);

    try {
      const data = await foodService.searchByBarcode(barcode);
      setResult(data);
      return data;
    } catch (err) {
      const errorMessage = err.message || 'Error al buscar código de barras';
      setError(errorMessage);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const reset = () => {
    setResult(null);
    setError(null);
  };

  return {
    result,
    loading,
    error,
    searchByBarcode,
    reset,
  };
};
```

---

## 🧩 Componentes de Ejemplo

### Lista de Alimentos

```javascript
import React from 'react';
import { useFoods } from '../hooks/useFoods';

const FoodList = () => {
  const { foods, loading, error, pagination, changePage, deleteFood } = useFoods();

  if (loading) return <div>Cargando...</div>;
  if (error) return <div>Error: {error}</div>;

  return (
    <div>
      <h2>Alimentos ({pagination.totalElements})</h2>
      
      <div className="food-grid">
        {foods.map((food) => (
          <div key={food.id} className="food-card">
            <h3>{food.name}</h3>
            {food.brand && <p className="brand">{food.brand}</p>}
            
            <div className="nutrition-info">
              <p><strong>Calorías:</strong> {food.nutritionalInfo?.calories} kcal</p>
              <p><strong>Proteínas:</strong> {food.nutritionalInfo?.protein}g</p>
              <p><strong>Carbohidratos:</strong> {food.nutritionalInfo?.carbohydrates}g</p>
              <p><strong>Grasas:</strong> {food.nutritionalInfo?.fats}g</p>
            </div>

            <div className="actions">
              <button onClick={() => handleEdit(food.id)}>Editar</button>
              <button onClick={() => deleteFood(food.id)}>Eliminar</button>
            </div>
          </div>
        ))}
      </div>

      {/* Paginación */}
      <div className="pagination">
        <button 
          disabled={pagination.page === 0}
          onClick={() => changePage(pagination.page - 1)}
        >
          Anterior
        </button>
        
        <span>
          Página {pagination.page + 1} de {pagination.totalPages}
        </span>
        
        <button 
          disabled={pagination.page >= pagination.totalPages - 1}
          onClick={() => changePage(pagination.page + 1)}
        >
          Siguiente
        </button>
      </div>
    </div>
  );
};

export default FoodList;
```

### Formulario de Crear Alimento

```javascript
import React, { useState } from 'react';
import { useFoods } from '../hooks/useFoods';

const CreateFoodForm = ({ onSuccess }) => {
  const { createFood, loading, error } = useFoods();
  
  const [formData, setFormData] = useState({
    name: '',
    brand: '',
    barcode: '',
    servingSize: '',
    servingUnit: 'g',
    nutritionalInfo: {
      calories: '',
      protein: '',
      carbohydrates: '',
      fats: '',
      fiber: '',
      sugars: '',
      saturatedFats: '',
      sodium: '',
      calcium: '',
      iron: '',
    }
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    
    if (name.startsWith('nutrition.')) {
      const nutritionField = name.split('.')[1];
      setFormData(prev => ({
        ...prev,
        nutritionalInfo: {
          ...prev.nutritionalInfo,
          [nutritionField]: value
        }
      }));
    } else {
      setFormData(prev => ({
        ...prev,
        [name]: value
      }));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    try {
      // Convertir strings a números
      const foodData = {
        ...formData,
        servingSize: parseFloat(formData.servingSize) || null,
        nutritionalInfo: {
          calories: parseFloat(formData.nutritionalInfo.calories) || null,
          protein: parseFloat(formData.nutritionalInfo.protein) || null,
          carbohydrates: parseFloat(formData.nutritionalInfo.carbohydrates) || null,
          fats: parseFloat(formData.nutritionalInfo.fats) || null,
          fiber: parseFloat(formData.nutritionalInfo.fiber) || null,
          sugars: parseFloat(formData.nutritionalInfo.sugars) || null,
          saturatedFats: parseFloat(formData.nutritionalInfo.saturatedFats) || null,
          sodium: parseFloat(formData.nutritionalInfo.sodium) || null,
          calcium: parseFloat(formData.nutritionalInfo.calcium) || null,
          iron: parseFloat(formData.nutritionalInfo.iron) || null,
        }
      };

      await createFood(foodData);
      alert('Alimento creado exitosamente');
      onSuccess?.();
      
      // Resetear formulario
      setFormData({
        name: '',
        brand: '',
        barcode: '',
        servingSize: '',
        servingUnit: 'g',
        nutritionalInfo: {
          calories: '', protein: '', carbohydrates: '', fats: '',
          fiber: '', sugars: '', saturatedFats: '', sodium: '',
          calcium: '', iron: '',
        }
      });
    } catch (err) {
      console.error('Error al crear alimento:', err);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <h2>Crear Nuevo Alimento</h2>
      
      {error && <div className="error">{error}</div>}

      {/* Información básica */}
      <div className="form-section">
        <h3>Información Básica</h3>
        
        <input
          type="text"
          name="name"
          placeholder="Nombre del alimento *"
          value={formData.name}
          onChange={handleChange}
          required
        />

        <input
          type="text"
          name="brand"
          placeholder="Marca"
          value={formData.brand}
          onChange={handleChange}
        />

        <input
          type="text"
          name="barcode"
          placeholder="Código de barras"
          value={formData.barcode}
          onChange={handleChange}
        />

        <div className="serving-info">
          <input
            type="number"
            name="servingSize"
            placeholder="Tamaño de porción"
            value={formData.servingSize}
            onChange={handleChange}
            step="0.01"
          />
          
          <select
            name="servingUnit"
            value={formData.servingUnit}
            onChange={handleChange}
          >
            <option value="g">gramos (g)</option>
            <option value="ml">mililitros (ml)</option>
            <option value="oz">onzas (oz)</option>
            <option value="cup">tazas</option>
          </select>
        </div>
      </div>

      {/* Macronutrientes */}
      <div className="form-section">
        <h3>Macronutrientes (por porción)</h3>
        
        <input
          type="number"
          name="nutrition.calories"
          placeholder="Calorías (kcal)"
          value={formData.nutritionalInfo.calories}
          onChange={handleChange}
          step="0.01"
        />

        <input
          type="number"
          name="nutrition.protein"
          placeholder="Proteínas (g)"
          value={formData.nutritionalInfo.protein}
          onChange={handleChange}
          step="0.01"
        />

        <input
          type="number"
          name="nutrition.carbohydrates"
          placeholder="Carbohidratos (g)"
          value={formData.nutritionalInfo.carbohydrates}
          onChange={handleChange}
          step="0.01"
        />

        <input
          type="number"
          name="nutrition.fats"
          placeholder="Grasas (g)"
          value={formData.nutritionalInfo.fats}
          onChange={handleChange}
          step="0.01"
        />

        <input
          type="number"
          name="nutrition.fiber"
          placeholder="Fibra (g)"
          value={formData.nutritionalInfo.fiber}
          onChange={handleChange}
          step="0.01"
        />

        <input
          type="number"
          name="nutrition.sugars"
          placeholder="Azúcares (g)"
          value={formData.nutritionalInfo.sugars}
          onChange={handleChange}
          step="0.01"
        />
      </div>

      {/* Micronutrientes */}
      <div className="form-section">
        <h3>Micronutrientes (opcional)</h3>
        
        <input
          type="number"
          name="nutrition.sodium"
          placeholder="Sodio (mg)"
          value={formData.nutritionalInfo.sodium}
          onChange={handleChange}
          step="0.01"
        />

        <input
          type="number"
          name="nutrition.calcium"
          placeholder="Calcio (mg)"
          value={formData.nutritionalInfo.calcium}
          onChange={handleChange}
          step="0.01"
        />

        <input
          type="number"
          name="nutrition.iron"
          placeholder="Hierro (mg)"
          value={formData.nutritionalInfo.iron}
          onChange={handleChange}
          step="0.01"
        />
      </div>

      <button type="submit" disabled={loading}>
        {loading ? 'Creando...' : 'Crear Alimento'}
      </button>
    </form>
  );
};

export default CreateFoodForm;
```

### Búsqueda por Código de Barras

```javascript
import React, { useState } from 'react';
import { useBarcodeSearch } from '../hooks/useBarcodeSearch';

const BarcodeScanner = ({ onFoodFound }) => {
  const [barcode, setBarcode] = useState('');
  const { result, loading, error, searchByBarcode, reset } = useBarcodeSearch();

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!barcode.trim()) return;

    try {
      const data = await searchByBarcode(barcode);
      onFoodFound?.(data);
    } catch (err) {
      console.error('Error:', err);
    }
  };

  return (
    <div className="barcode-scanner">
      <h2>Buscar por Código de Barras</h2>
      
      <form onSubmit={handleSearch}>
        <input
          type="text"
          placeholder="Ingresa el código de barras"
          value={barcode}
          onChange={(e) => setBarcode(e.target.value)}
          disabled={loading}
        />
        
        <button type="submit" disabled={loading || !barcode.trim()}>
          {loading ? 'Buscando...' : 'Buscar'}
        </button>
        
        {result && (
          <button type="button" onClick={() => { reset(); setBarcode(''); }}>
            Limpiar
          </button>
        )}
      </form>

      {error && (
        <div className="error-message">
          {error}
        </div>
      )}

      {result && (
        <div className="search-result">
          {result.foundInDatabase ? (
            <div className="success-badge">
              ✓ Encontrado en base de datos local
            </div>
          ) : result.source === 'openfoodfacts' ? (
            <div className="info-badge">
              ℹ Encontrado en Open Food Facts
            </div>
          ) : (
            <div className="warning-badge">
              ✗ No encontrado
            </div>
          )}

          {result.food && (
            <div className="food-details">
              <h3>{result.food.name}</h3>
              {result.food.brand && <p><strong>Marca:</strong> {result.food.brand}</p>}
              
              <div className="nutrition-summary">
                <p><strong>Calorías:</strong> {result.food.nutritionalInfo?.calories} kcal</p>
                <p><strong>Proteínas:</strong> {result.food.nutritionalInfo?.protein}g</p>
                <p><strong>Carbohidratos:</strong> {result.food.nutritionalInfo?.carbohydrates}g</p>
                <p><strong>Grasas:</strong> {result.food.nutritionalInfo?.fats}g</p>
              </div>

              {result.source === 'openfoodfacts' && (
                <button onClick={() => saveToDatabase(result.food)}>
                  Guardar en mi base de datos
                </button>
              )}
            </div>
          )}

          {result.message && !result.food && (
            <p className="no-results">{result.message}</p>
          )}
        </div>
      )}
    </div>
  );
};

export default BarcodeScanner;
```

### Barra de Búsqueda

```javascript
import React, { useState } from 'react';
import { useFoods } from '../hooks/useFoods';

const SearchBar = () => {
  const [query, setQuery] = useState('');
  const { searchFoods, loading } = useFoods();

  const handleSearch = (e) => {
    e.preventDefault();
    if (query.trim()) {
      searchFoods(query);
    }
  };

  return (
    <form onSubmit={handleSearch} className="search-bar">
      <input
        type="text"
        placeholder="Buscar por nombre o marca..."
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        disabled={loading}
      />
      <button type="submit" disabled={loading || !query.trim()}>
        {loading ? 'Buscando...' : 'Buscar'}
      </button>
    </form>
  );
};

export default SearchBar;
```

---

## 🚨 Manejo de Errores

### Componente de Error

```javascript
import React from 'react';

const ErrorDisplay = ({ error, onRetry }) => {
  if (!error) return null;

  const getErrorMessage = (error) => {
    if (typeof error === 'string') return error;
    if (error.message) return error.message;
    if (error.validationErrors) {
      return Object.entries(error.validationErrors)
        .map(([field, msg]) => `${field}: ${msg}`)
        .join(', ');
    }
    return 'Ha ocurrido un error inesperado';
  };

  return (
    <div className="error-container">
      <div className="error-icon">⚠️</div>
      <p className="error-message">{getErrorMessage(error)}</p>
      {onRetry && (
        <button onClick={onRetry} className="retry-button">
          Reintentar
        </button>
      )}
    </div>
  );
};

export default ErrorDisplay;
```

---

## 📘 TypeScript Definitions

Si usas TypeScript, crea `src/types/food.ts`:

```typescript
export interface NutritionalInfo {
  id?: number;
  calories?: number;
  protein?: number;
  carbohydrates?: number;
  fats?: number;
  fiber?: number;
  sugars?: number;
  saturatedFats?: number;
  sodium?: number;
  calcium?: number;
  iron?: number;
  potassium?: number;
  vitaminA?: number;
  vitaminC?: number;
  vitaminD?: number;
  vitaminE?: number;
  vitaminB12?: number;
}

export interface Food {
  id?: number;
  name: string;
  brand?: string;
  barcode?: string;
  servingSize?: number;
  servingUnit?: string;
  nutritionalInfo?: NutritionalInfo;
  createdAt?: string;
  updatedAt?: string;
}

export interface FoodRequest {
  name: string;
  brand?: string;
  barcode?: string;
  servingSize?: number;
  servingUnit?: string;
  nutritionalInfo: NutritionalInfo;
}

export interface Page<T> {
  content: T[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface BarcodeSearchResponse {
  foundInDatabase: boolean;
  source: 'local' | 'openfoodfacts' | 'none';
  food?: Food;
  message?: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
}

export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  validationErrors?: Record<string, string>;
}
```

---

## 🎨 Ejemplo de CSS

```css
/* food-list.css */
.food-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
  padding: 1rem;
}

.food-card {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 1.5rem;
  background: white;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  transition: transform 0.2s;
}

.food-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.15);
}

.food-card h3 {
  margin: 0 0 0.5rem 0;
  color: #333;
}

.food-card .brand {
  color: #666;
  font-size: 0.9rem;
  margin-bottom: 1rem;
}

.nutrition-info {
  background: #f5f5f5;
  padding: 1rem;
  border-radius: 4px;
  margin: 1rem 0;
}

.nutrition-info p {
  margin: 0.5rem 0;
  font-size: 0.9rem;
}

.actions {
  display: flex;
  gap: 0.5rem;
  margin-top: 1rem;
}

.actions button {
  flex: 1;
  padding: 0.5rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
}

.actions button:first-child {
  background: #4CAF50;
  color: white;
}

.actions button:last-child {
  background: #f44336;
  color: white;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  margin: 2rem 0;
}

.pagination button {
  padding: 0.5rem 1rem;
  border: 1px solid #ddd;
  background: white;
  border-radius: 4px;
  cursor: pointer;
}

.pagination button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.error-message {
  background: #ffebee;
  color: #c62828;
  padding: 1rem;
  border-radius: 4px;
  margin: 1rem 0;
}

.success-badge {
  background: #4CAF50;
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  display: inline-block;
  margin-bottom: 1rem;
}

.info-badge {
  background: #2196F3;
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  display: inline-block;
  margin-bottom: 1rem;
}

.warning-badge {
  background: #ff9800;
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  display: inline-block;
  margin-bottom: 1rem;
}
```

---

## 📝 Ejemplo de Uso Completo

```javascript
// App.js
import React, { useState } from 'react';
import FoodList from './components/FoodList';
import CreateFoodForm from './components/CreateFoodForm';
import BarcodeScanner from './components/BarcodeScanner';
import SearchBar from './components/SearchBar';

function App() {
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [showScanner, setShowScanner] = useState(false);

  return (
    <div className="app">
      <header>
        <h1>Nutrition Tracker</h1>
        <div className="header-actions">
          <button onClick={() => setShowCreateForm(!showCreateForm)}>
            {showCreateForm ? 'Ver Lista' : 'Crear Alimento'}
          </button>
          <button onClick={() => setShowScanner(!showScanner)}>
            {showScanner ? 'Cerrar Scanner' : 'Escanear Código'}
          </button>
        </div>
      </header>

      <main>
        {showScanner && (
          <BarcodeScanner 
            onFoodFound={(data) => {
              console.log('Alimento encontrado:', data);
              setShowScanner(false);
            }}
          />
        )}

        {showCreateForm ? (
          <CreateFoodForm 
            onSuccess={() => setShowCreateForm(false)}
          />
        ) : (
          <>
            <SearchBar />
            <FoodList />
          </>
        )}
      </main>
    </div>
  );
}

export default App;
```

---

## ✅ Checklist de Integración

- [ ] Instalar axios
- [ ] Configurar variables de entorno
- [ ] Crear servicio API base
- [ ] Crear servicio de alimentos
- [ ] Implementar hooks personalizados
- [ ] Crear componentes de UI
- [ ] Añadir manejo de errores
- [ ] Probar todas las funcionalidades
- [ ] Añadir loading states
- [ ] Implementar paginación
- [ ] Añadir búsqueda
- [ ] Integrar scanner de códigos de barras

---

## 🚀 Próximos Pasos

Cuando el backend implemente autenticación:

1. **Actualizar interceptor de axios** para incluir JWT token
2. **Crear servicio de autenticación** (login, register, logout)
3. **Implementar context de usuario** para gestión de estado global
4. **Proteger rutas** con React Router
5. **Añadir refresh token** logic

¡Tu aplicación React está lista para consumir la API de Nutrition Tracker! 🎉
