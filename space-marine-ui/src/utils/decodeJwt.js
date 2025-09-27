export function decodeJwt(token) {
    if (!token) return null;
    try {
        const payload = token.split('.')[1]; // берем вторую часть JWT
        const decoded = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
        return JSON.parse(decoded);
    } catch (e) {
        console.error('Failed to decode JWT', e);
        return null;
    }
}