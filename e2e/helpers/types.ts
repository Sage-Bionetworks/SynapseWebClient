/* USERS */
export type TestUser = {
  username: string
  email: string
  password: string
  tou: boolean // true if user agrees to Synapse terms of use
  validatedUser: boolean // true if a verification approved state should be added for the user
}

// Note: many other fields are included in the payload, but only including
// field used by e2e tests here
export type JwtPayload = {
  sub: string
}

/* ENTITIES */
export type Project = {
  name: string
  id: string
}

export type FileType = 'text/txt' | 'text/csv' | 'application/json'
