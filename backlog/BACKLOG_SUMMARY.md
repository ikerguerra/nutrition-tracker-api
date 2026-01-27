# 📋 Nutrition Tracker - Resumen del Backlog

## 📊 Estadísticas Generales

- **Total de EPICs:** 9
- **Total de User Stories:** 100+
- **Duración estimada total:** 28-38 semanas (~7-9 meses)
- **Estado actual:** Fase 0 completada (fundamentos básicos)

---

## 🎯 EPICs por Prioridad

### 🔴 ALTA Prioridad (Fase 1)
1. **EPIC 1: Autenticación y Usuarios** - 17 US
2. **EPIC 2: Navegación Temporal** - 6 US

### 🟡 MEDIA Prioridad (Fase 2)
3. **EPIC 3: Favoritos y Frecuentes** - 9 US
4. **EPIC 4: Recomendaciones de Dietas** - 9 US
5. **EPIC 6: Calendario de Cumplimiento** - 8 US
6. **EPIC 7: Desglose de Macronutrientes** - 8 US
7. **EPIC 8: Internacionalización** - 7 US

### 🟢 BAJA Prioridad (Fase 3-5)
8. **EPIC 5: Rediseño Frontend** - 6 US
9. **EPIC 9: Funcionalidades Avanzadas** - 22 US

---

## ✨ Características Destacadas Confirmadas

### 🔐 Autenticación
- ✅ Login con email/password
- ✅ OAuth2 con Google
- ✅ OAuth2 con Facebook
- ✅ JWT para sesiones

### 👤 Perfil de Usuario
- ✅ Datos personales (altura, peso, edad, género)
- ✅ Objetivos nutricionales (perder peso, ganar músculo, etc.)
- ✅ Tipos de dieta específicos (cetogénica, vegana, vegetariana, paleo, etc.)
- ✅ Sistema de unidades (métrico/imperial)
- ✅ Cálculo automático de calorías diarias
- ✅ Macronutrientes personalizables con presets guardables

### 📅 Seguimiento Diario
- ✅ Registro de comidas por tipo (Desayuno, Almuerzo, Cena, Snacks)
- ✅ Navegación entre días con efecto carrusel
- ✅ Registro de peso diario
- ✅ Gráfico de tendencia de peso

### ⭐ Alimentos
- ✅ Sistema de favoritos
- ✅ Alimentos frecuentes (top 10 más consumidos)
- ✅ Alimentos recientes (últimos 14 días)
- ✅ Buscador unificado con filtros

### 🤖 Recomendaciones Inteligentes
- ✅ Análisis de patrones alimenticios
- ✅ Generación automática de dietas diarias
- ✅ Límite de variedad (no repetir alimentos en el mismo día)
- ✅ Sistema de feedback para mejorar recomendaciones

### 📆 Calendario
- ✅ Vista mensual con indicadores de cumplimiento
- ✅ Verde: objetivo alcanzado
- ✅ Rojo: objetivo no alcanzado
- ✅ Mostrar peso registrado cada día
- ✅ Estadísticas del mes (racha, días cumplidos)

### 📊 Análisis Nutricional
- ✅ Desglose por comida, alimento y macro
- ✅ Gráficos de distribución
- ✅ Micronutrientes con % de RDA (Recommended Daily Allowance)
- ✅ Alertas de deficiencias nutricionales

### 🌍 Internacionalización
- ✅ Soporte español e inglés
- ✅ Selector de idioma
- ✅ Localización de fechas y números

### 🎮 Gamificación
- ✅ Sistema de logros y badges
- ✅ Rachas de días cumpliendo objetivos
- ✅ Niveles y XP
- ✅ Compartir logros en redes sociales

### 🍽️ Plantillas y Recetas
- ✅ Guardar combinaciones de alimentos como plantillas
- ✅ Quick add de comidas completas
- ✅ Sistema de recetas compartibles
- ✅ Marketplace de recetas de la comunidad
- ✅ Cálculo automático de macros por porción

### 📱 PWA y Notificaciones
- ✅ Modo offline con Service Worker
- ✅ Push notifications
- ✅ Recordatorios para registrar comidas

### 🔗 Integraciones
- ✅ Google Fit (importar calorías quemadas)
- ✅ Apple Health (iOS)

### 🔍 Búsqueda Avanzada
- ✅ Elasticsearch para búsqueda full-text
- ✅ Búsqueda fuzzy (tolerante a errores)
- ✅ Búsqueda por ingredientes
- ✅ Autocompletado inteligente

### 📈 Estadísticas y Reportes
- ✅ Gráficos de tendencias semanales/mensuales
- ✅ Exportar a PDF y CSV
- ✅ Análisis de cumplimiento de objetivos

### 🧪 Calidad y Testing
- ✅ Tests automatizados (cobertura 80%+)
- ✅ CI/CD con GitHub Actions
- ✅ Monitoreo con Grafana y Sentry

---

## 🚀 Próximos Pasos

### Sprint 1-2 (Semanas 1-4)
- [ ] Implementar entidad User y UserProfile
- [ ] Configurar Spring Security con JWT
- [ ] Implementar OAuth2 con Google
- [ ] Crear páginas de login/registro en frontend
- [ ] Implementar perfil de usuario

### Sprint 3-4 (Semanas 5-8)
- [ ] Cálculo de calorías diarias según objetivo
- [ ] Sistema de macronutrientes personalizables
- [ ] Presets de macros
- [ ] Tipos de dieta específicos
- [ ] Sistema de unidades métrico/imperial

### Sprint 5-6 (Semanas 9-12)
- [ ] Navegación temporal con carrusel
- [ ] Sistema de favoritos
- [ ] Alimentos frecuentes y recientes
- [ ] Buscador unificado

---

## 📝 Notas Importantes

### Decisiones de Diseño
- **Almacenamiento:** Siempre en métrico en BD, conversión en presentación
- **Macros:** Suma de porcentajes debe ser exactamente 100%
- **Recomendaciones:** Máximo 1 repetición del mismo alimento por día (configurable)
- **Cumplimiento:** Margen de tolerancia ±5% para objetivos calóricos
- **Idioma:** Español por defecto, detección automática del navegador

### Consideraciones Técnicas
- **Paginación:** Obligatoria en todos los listados
- **Índices:** Crear en columnas frecuentemente consultadas
- **Caché:** Redis para alimentos frecuentes y cálculos
- **Búsqueda:** Elasticsearch para queries complejas
- **Tests:** Cobertura mínima 80% antes de merge

---

## 📚 Documentación

- **[BACKLOG.md](./BACKLOG.md)** - Backlog completo con todas las User Stories detalladas
- **[ROADMAP.md](./ROADMAP.md)** - Roadmap con fases, timelines y métricas de éxito

---

**Creado:** 2025-12-01  
**Última actualización:** 2025-12-01
