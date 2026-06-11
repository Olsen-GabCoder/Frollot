export const radius = {
  xs: 4,
  sm: 8,
  md: 12,
  lg: 16,
  xl: 28,
  full: 999,
} as const;

export const spacing = {
  xs: 4,    // sp-1
  sm: 8,    // sp-2
  md: 12,   // sp-3
  lg: 16,   // sp-4
  xl: 20,   // sp-5
  '2xl': 24, // sp-6
  '3xl': 32, // sp-8
  '4xl': 40, // sp-10
  '5xl': 48, // sp-12
  '6xl': 64, // sp-16
} as const;

export const elevation = {
  0: {
    shadowColor: 'rgb(39, 26, 44)',
    shadowOffset: { width: 0, height: 0 },
    shadowOpacity: 0,
    shadowRadius: 0,
    elevation: 0,
  },
  1: {
    shadowColor: 'rgb(39, 26, 44)',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.08,
    shadowRadius: 3,
    elevation: 1,
  },
  2: {
    shadowColor: 'rgb(39, 26, 44)',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 6,
    elevation: 2,
  },
  3: {
    shadowColor: 'rgb(39, 26, 44)',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.12,
    shadowRadius: 8,
    elevation: 3,
  },
  4: {
    shadowColor: 'rgb(39, 26, 44)',
    shadowOffset: { width: 0, height: 6 },
    shadowOpacity: 0.14,
    shadowRadius: 10,
    elevation: 4,
  },
  5: {
    shadowColor: 'rgb(39, 26, 44)',
    shadowOffset: { width: 0, height: 8 },
    shadowOpacity: 0.16,
    shadowRadius: 12,
    elevation: 5,
  },
} as const;
