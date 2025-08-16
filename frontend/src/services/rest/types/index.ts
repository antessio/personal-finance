
export interface UploadFilRest {
  id: string;
  filePath: string;
  sourceType: string;
  status: string;
  userOwner: string;
  insertedAt: string;
  updatedAt?: string;
} 

export interface Account{
  id: string;
  name: string;
}

export interface CategoryRest {
    id: string;
    name: string;
    macroCategory: string;
    emoji: string;
    userOwner: string;
    matchers: string[];
    insertedAt: string;
    updatedAt?: string;
}

export interface TransactionRest{
    id: string;
    date: string;
    description: string;
    category?: CategoryRest;
    amount: number;
    source: string; 
    account: string;
    skip: boolean;
    userOwner: string;
    insertedAt: string;
    updatedAt?: string;
}