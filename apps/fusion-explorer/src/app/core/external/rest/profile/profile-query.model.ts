type ProfileResultOptionalField =
    | "emails" 
    | "permissions"
    | "organization"
    | "user"

export interface ProfileQuery {
    id: string[],
    alias: string[],
    lastName: string[],
    firstName: string[],
    emails: string[],
    lastLogin: [string, Date][],
    limit: number,
    offset: number,
    createdAt: [string, Date][],
    updatedAt: [string, Date][],
    orderBy: [string, number][],
    include: ProfileResultOptionalField[]
}