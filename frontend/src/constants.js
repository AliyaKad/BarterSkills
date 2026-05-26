export const CATEGORIES = [
    { value: '', label: 'Все' },
    { value: 'EDUCATION', label: 'Образование' },
    { value: 'IT', label: 'IT' },
    { value: 'HOUSEHOLD', label: 'Быт' },
    { value: 'CREATIVE', label: 'Творчество' },
    { value: 'TRANSLATION', label: 'Переводы' },
    { value: 'LEGAL', label: 'Юриспруденция' },
    { value: 'OTHER', label: 'Другое' },
];

export const CATEGORY_LABELS = Object.fromEntries(
    CATEGORIES.filter(c => c.value).map(c => [c.value, c.label])
);

export const DEAL_STATUS_LABELS = {
    PROPOSED: 'Предложена',
    ACCEPTED: 'Принята',
    IN_PROGRESS: 'В работе',
    COMPLETED: 'Завершена',
    DISPUTED: 'Спор',
    CANCELLED: 'Отменена',
};

export const TX_TYPE_LABELS = {
    HOLD: 'Заморозка',
    TRANSFER: 'Перевод',
    REFUND: 'Возврат',
    BONUS: 'Бонус',
};
