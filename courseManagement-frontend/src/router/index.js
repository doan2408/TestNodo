import { createRouter, createWebHistory } from 'vue-router';
import StudentPage from '@/views/students/StudentPage.vue';
import CoursePage from '@/views/courses/CoursePage.vue';
import LessonPage from '@/views/lessons/LessonPage.vue';
import EnrollmentPage from '@/views/enrollments/EnrollmentPage.vue';

const routes = [
  {
    path: '/',
    component: StudentPage
  },
  {
    path: '/students',
    name: 'StudentPage',
    component: StudentPage,
  },
  {
    path: '/courses',
    component: CoursePage,
    children: [
      {
        path: '',
        name: 'CourseList',
        component: CoursePage
      }
    ]
  },
  {
    path: '/courses/:courseId/lessons',
    name: 'LessonList',
    component: LessonPage,
    meta: { requiresAuth: true }
  },
  {
    path: '/courses/:courseId/students',
    name: 'StudentList',
    component: StudentPage
  },
  {
    path: '/enrollments',
    name: "Enrollments",
    component: EnrollmentPage
  },
  {
    path: "/:pathMatch(.*)*",
    name: "NotFound",
    component: () => import("@/views/NotFound.vue")
  }

];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
});

export default router;
