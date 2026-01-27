# 📋 Nutrition Tracker - Documentación del Proyecto

Esta carpeta contiene toda la documentación de planificación y gestión del proyecto **Nutrition Tracker**.

---

## 📁 Estructura de Archivos

### 📄 **BACKLOG.md** (28.7 KB)
Backlog completo del proyecto con todas las User Stories organizadas en 9 EPICs.

**Contenido:**
- Estado actual del proyecto (funcionalidades implementadas)
- 9 EPICs con 100+ User Stories
- Cada US separada en tareas de Backend y Frontend
- Decisiones confirmadas documentadas
- Buenas prácticas de desarrollo
- Consideraciones de escalabilidad

**Uso:**
- Consultar para planificar sprints
- Marcar tareas completadas con ✅
- Añadir nuevas User Stories siguiendo el formato establecido

---

### 🗺️ **ROADMAP.md** (8.9 KB)
Roadmap de desarrollo con fases, timelines y métricas.

**Contenido:**
- 5 fases de desarrollo (28-38 semanas estimadas)
- Objetivos y entregables por fase
- KPIs técnicos y de producto
- Stack tecnológico completo
- Proceso de desarrollo (Scrum, workflow, definición de "Done")

**Uso:**
- Planificación a largo plazo
- Comunicación con stakeholders
- Seguimiento de progreso general

---

### 📊 **BACKLOG_SUMMARY.md** (5.6 KB)
Resumen ejecutivo rápido del backlog.

**Contenido:**
- Estadísticas generales (EPICs, User Stories, duración)
- EPICs por prioridad
- Características destacadas confirmadas
- Próximos pasos (Sprints 1-6)
- Decisiones de diseño importantes

**Uso:**
- Vista rápida del proyecto
- Onboarding de nuevos desarrolladores
- Referencia rápida de funcionalidades

---

## 🎯 EPICs del Proyecto

1. **EPIC 1:** Autenticación y Usuarios (17 US) - 🔴 ALTA
2. **EPIC 2:** Navegación Temporal (6 US) - 🔴 ALTA
3. **EPIC 3:** Favoritos y Frecuentes (9 US) - 🟡 MEDIA
4. **EPIC 4:** Recomendaciones de Dietas (9 US) - 🟡 MEDIA
5. **EPIC 5:** Rediseño Frontend (6 US) - 🟢 BAJA
6. **EPIC 6:** Calendario de Cumplimiento (8 US) - 🟡 MEDIA
7. **EPIC 7:** Desglose de Macronutrientes (8 US) - 🟡 MEDIA
8. **EPIC 8:** Internacionalización (7 US) - 🟡 MEDIA
9. **EPIC 9:** Funcionalidades Avanzadas (22 US) - 🟢 BAJA

---

## 🔄 Cómo Actualizar el Backlog

### Marcar Tareas Completadas
```markdown
- [x] **US-X.Y: Título de la tarea**  ✅
```

### Añadir Nuevas User Stories
```markdown
- [ ] **US-X.Y: Título descriptivo**
  - Descripción de la funcionalidad
  - Criterios de aceptación
  - Notas técnicas si aplica
```

### Añadir Nuevos EPICs
```markdown
### 🎯 EPIC X: Nombre del EPIC

> **Prioridad:** ALTA/MEDIA/BAJA  
> **Objetivo:** Descripción del objetivo

#### Backend Tasks
- [ ] **US-X.1: ...**

#### Frontend Tasks
- [ ] **US-X.Y: ...**
```

---

## 📍 Ubicación en el Workspace

```
c:\Users\ikerg\.gemini\antigravity\scratch\
├── backlog/                          # 📂 Documentación centralizada
│   ├── BACKLOG.md                    # Backlog completo
│   ├── ROADMAP.md                    # Roadmap de desarrollo
│   ├── BACKLOG_SUMMARY.md            # Resumen ejecutivo
│   └── README.md                     # Este archivo
├── nutrition-tracker-api/            # Backend (Spring Boot)
└── nutrition-tracker-pwa/            # Frontend (React)
```

---

## 🚀 Próximos Pasos

### Sprint 1-2 (Semanas 1-4)
Comenzar con **EPIC 1: Autenticación y Usuarios**

**Backend:**
- [ ] US-1.1: Crear entidad User
- [ ] US-1.2: Implementar Spring Security con JWT
- [ ] US-1.3: Crear UserProfile entity
- [ ] US-1.9: OAuth2 con Google

**Frontend:**
- [ ] US-1.11: Páginas de autenticación
- [ ] US-1.12: Botones de OAuth2
- [ ] US-1.13: Gestión de tokens

---

## 📚 Recursos Adicionales

### Backend
- [README.md](../nutrition-tracker-api/README.md) - Documentación técnica del API
- [QUICKSTART.md](../nutrition-tracker-api/QUICKSTART.md) - Guía de inicio rápido
- [REACT_INTEGRATION.md](../nutrition-tracker-api/REACT_INTEGRATION.md) - Integración con React

### Frontend
- [package.json](../nutrition-tracker-pwa/package.json) - Dependencias del proyecto
- [vite.config.ts](../nutrition-tracker-pwa/vite.config.ts) - Configuración de Vite

---

## 🤝 Contribución

1. Consultar **BACKLOG.md** para ver tareas disponibles
2. Asignar una User Story
3. Crear branch: `feature/US-X.Y-descripcion`
4. Implementar siguiendo buenas prácticas
5. Actualizar backlog marcando tarea como completada
6. Crear Pull Request

---

## 📧 Contacto

Para preguntas sobre el backlog o priorización:
- **Email:** support@nutritiontracker.com
- **GitHub Issues:** Para proponer nuevas features

---

**Creado:** 2025-12-01  
**Última actualización:** 2025-12-01  
**Próxima revisión:** 2025-12-15
