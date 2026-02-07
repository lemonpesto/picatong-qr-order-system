const ApiUtils = (() => {
    const safeJson = (str) => {
        try { return JSON.parse(str); } catch { return null; }
    };

    const readErrorMessage = async (res) => {
        const raw = await res.text().catch(() => '');
        if (!raw) return '';
        const parsed = safeJson(raw);
        if (parsed?.message) return String(parsed.message);
        return raw;
    };

    return {
        safeJson,
        readErrorMessage,
    };
})();