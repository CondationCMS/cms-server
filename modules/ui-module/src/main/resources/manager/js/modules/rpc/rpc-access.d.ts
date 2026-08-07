export interface Permission {
    key: string;
    description: string;
}
export interface Role {
    id: string;
    name: string;
    permissions: string[];
}
export interface ManagerUser {
    username: string;
    mail: string;
    roles: string[];
}
export declare const listPermissions: () => Promise<Permission[]>;
export declare const listRoles: () => Promise<Role[]>;
export declare const saveRole: (role: Role) => Promise<Role>;
export declare const deleteRole: (id: string) => Promise<boolean>;
export declare const listUsers: () => Promise<ManagerUser[]>;
export declare const createUser: (user: ManagerUser, password: string) => Promise<ManagerUser>;
export declare const updateUser: (user: ManagerUser, password?: string) => Promise<ManagerUser>;
export declare const deleteUser: (username: string) => Promise<boolean>;
