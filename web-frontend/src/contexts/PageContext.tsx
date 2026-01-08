import React, { createContext, useContext, useState, ReactNode } from 'react';

interface PageContextType {
    pageTitle: string;
    setPageTitle: (title: string) => void;
}

const PageContext = createContext<PageContextType | undefined>(undefined);

export const PageProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
    const [pageTitle, setPageTitle] = useState<string>('');

    return (
        <PageContext.Provider value={{ pageTitle, setPageTitle }}>
            {children}
        </PageContext.Provider>
    );
};

export const usePageContext = () => {
    const context = useContext(PageContext);
    if (!context) {
        throw new Error('usePageContext must be used within a PageProvider');
    }
    return context;
};
