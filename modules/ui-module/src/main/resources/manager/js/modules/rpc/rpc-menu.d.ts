export type MenuItemType = 'link' | 'heading' | 'divider';
export interface MenuItem {
    id: string;
    type: MenuItemType;
    label: string;
    url: string;
    target: '_self' | '_blank';
    enabled: boolean;
    children: MenuItem[];
}
export interface Menu {
    id: string;
    name: string;
    items: MenuItem[];
}
export declare const listMenus: () => Promise<Menu[]>;
export declare const getMenu: (id: string) => Promise<Menu>;
export declare const createMenu: (menu: Menu) => Promise<Menu>;
export declare const updateMenu: (menu: Menu) => Promise<Menu>;
export declare const deleteMenu: (id: string) => Promise<boolean>;
